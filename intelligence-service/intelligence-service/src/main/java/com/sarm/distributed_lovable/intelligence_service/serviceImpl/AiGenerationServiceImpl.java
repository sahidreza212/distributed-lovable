package com.sarm.distributed_lovable.intelligence_service.serviceImpl;


import com.sarm.distributed_lovable.common_lib.enums.ChatEventStatus;
import com.sarm.distributed_lovable.common_lib.enums.ChatEventType;
import com.sarm.distributed_lovable.common_lib.enums.MessageRole;
import com.sarm.distributed_lovable.common_lib.events.FileStoreRequestEvent;
import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import com.sarm.distributed_lovable.intelligence_service.client.WorkspaceClient;
import com.sarm.distributed_lovable.intelligence_service.dtos.StreamResponse;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatEvent;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatMessage;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatSection;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatSessionId;
import com.sarm.distributed_lovable.intelligence_service.llm.LlmResponseParse;
import com.sarm.distributed_lovable.intelligence_service.llm.PromptUtils;
import com.sarm.distributed_lovable.intelligence_service.llm.advisor.FileTreeContextAdvisor;
import com.sarm.distributed_lovable.intelligence_service.llm.tools.CodeGenerationTools;
import com.sarm.distributed_lovable.intelligence_service.repository.ChatEventRepository;
import com.sarm.distributed_lovable.intelligence_service.repository.ChatMessageRepository;
import com.sarm.distributed_lovable.intelligence_service.repository.ChatSessionRepository;
import com.sarm.distributed_lovable.intelligence_service.service.AiGenerationService;
import com.sarm.distributed_lovable.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;



@Service
@Slf4j
@RequiredArgsConstructor
public class AiGenerationServiceImpl implements AiGenerationService {

    private  final ChatClient chatClient;
    private final AuthUtil authUtil;
    private  final FileTreeContextAdvisor fileTreeContextAdvisor;
    private  final ChatSessionRepository chatSessionRepository;
    private  final LlmResponseParse llmResponseParse;
    private  final ChatMessageRepository chatMessageRepository;
    private final ChatEventRepository chatEventRepository;
    private  final UsageService usageService;
    private final WorkspaceClient workspaceClient;
    private  final KafkaTemplate<String ,Object>kafkaTemplate;


    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<StreamResponse> streamResponse(String userMessage, Long projectId) {

      //  usageService.checkDailyTokensUsage();

        log.info("AI STREAM START -> message={}, projectId={}", userMessage, projectId);
        log.info("ChatClient Bean -> {}", chatClient);

        Long userId = authUtil.getCurrentUserId();
        ChatSection chatSection =   createChatSessionIfNotExists(projectId, userId);

        Map<String, Object> adviserParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );

        StringBuilder fullResponse = new StringBuilder();
        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectId,workspaceClient);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long>endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();


        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTools)
                .advisors(advisorSpec -> {
                    advisorSpec.params(adviserParams);
                    advisorSpec.advisors(fileTreeContextAdvisor);
                })
                .stream()
                .chatResponse()

                .doOnNext(response->{

                   if(response.getResult() != null && !response.getResults().isEmpty()) {
                       String content = response.getResult().getOutput().getText();
                       log.info("TOKEN -> {}", content);
                       if(content != null && !content.isEmpty() && endTime.get() == 0){
                           endTime.set(System.currentTimeMillis());
                       }
                       if(response.getMetadata().getUsage() != null){
                           usageRef.set(response.getMetadata().getUsage());
                       }
                       fullResponse.append(content);
                   }

                })
                .doOnComplete(() -> {
                    log.info("STREAM COMPLETED");
                    Schedulers.boundedElastic().schedule(() -> {
//                           parseAndSaveFile(fullResponse.toString(), projectId)
                        long duration = (endTime.get() - startTime.get()) / 1000;
                        finalizeChats(userMessage, chatSection, fullResponse.toString(),duration,usageRef.get(),userId);
                    });
                })

                .doOnError(error ->
                        log.error("Error during streaming for projectId: {}", projectId))

                .map(response -> {

                    if(response.getResult() != null && !response.getResults().isEmpty()){
                        String text = response.getResult().getOutput().getText();
                        return new StreamResponse(text != null ? text : "");
                    }

                    return new StreamResponse("");
                });
    }

     private void finalizeChats (String userMessage, ChatSection chatSection, String fullText, Long duration, Usage usage ,Long userId){

         Long  projectId = chatSection.getId().getProjectId();

         if(usage != null){
             int totalTokens = usage.getTotalTokens();
             usageService.recordTokenUsage(chatSection.getId().getUserId(), totalTokens);
         }

        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSection(chatSection)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .tokensUsed(usage != null ? usage.getPromptTokens() : 0) // I have make some change from the real one
                        .build()
        );

        ChatMessage assistantChatMessage = ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .content("Assistant Message here....")    // I have make some change here
                .chatSection(chatSection)
                .tokensUsed(usage != null ? usage.getCompletionTokens() : 0)  // I have make some change from the real one
                .build();

        assistantChatMessage = chatMessageRepository.save(assistantChatMessage);


         List<ChatEvent>chatEventList = llmResponseParse.parseChatEvents(fullText,assistantChatMessage);
         chatEventList.addFirst(ChatEvent.builder()
                         .type(ChatEventType.THOUGHT)
                         .status(ChatEventStatus.CONFIRMED)
                         .chatMessage(assistantChatMessage)
                         .content("Thought for "+duration+"s")
                         .sequenceOrder(0)
                 .build()

         );
         chatEventList.stream()
                 .filter(e->e.getType() == ChatEventType.FILE_EDIT)
                 .forEach(e->{
            String sagaId = UUID.randomUUID().toString();
            e.setSagaId(sagaId);
                     FileStoreRequestEvent fileStoreRequestEvent = new FileStoreRequestEvent(
                             projectId,
                             sagaId,
                             e.getFilePath(),
                             e.getContent(),
                             userId
                     );

                     log.info("Storage request event sent :{} ",e.getFilePath());
                     kafkaTemplate.send("file-storage-request-event","project-"+projectId);
                 });

         chatEventRepository.saveAll(chatEventList);

     }


    private ChatSection createChatSessionIfNotExists(Long projectId, Long userId)  {

        ChatSessionId chatSessionId = new ChatSessionId(projectId,userId);
        ChatSection chatSection = chatSessionRepository.findById(chatSessionId).orElse(null);

        if(chatSection == null){
            chatSection = ChatSection.builder()
                    .id(chatSessionId)
                    .build();
            chatSection = chatSessionRepository.save(chatSection);
        }
        return  chatSection;

    }
}
