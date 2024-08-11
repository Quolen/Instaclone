package com.example.instaclone.controllers;

import com.example.instaclone.dto.ChatNameDTO;
import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import com.example.instaclone.security.JWTTokenProvider;
import com.example.instaclone.services.ChatService;
import com.example.instaclone.services.MessageService;
import com.example.instaclone.web.ChatController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableWebSocketMessageBroker
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ChatController chatController;

    @MockBean
    private JWTTokenProvider jwtTokenProvider;
    private String jwtToken;

    private WebSocketStompClient stompClient;
    private BlockingQueue<Message> blockingQueue;

    @LocalServerPort
    private int port;

    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    @BeforeEach
    public void setUp() {
        jwtToken = "mock-jwt-token";

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        given(userDetails.getUsername()).willReturn("testUser");

        Authentication authentication = Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authentication.isAuthenticated()).willReturn(true);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);

        WebSocketClient webSocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(
                List.of(new WebSocketTransport(webSocketClient))
        );
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        blockingQueue = new LinkedBlockingQueue<>();
    }

    @Test
    public void testSendMessage() throws Exception {
        String toUser = "user2";
        String sender = "user1";
        String content = "Hello, user2!";
        LocalDateTime timestamp = LocalDateTime.now();
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Chat chat = new Chat("chatName");
        chat.setChatId(1L); // Set a non-null ID for the chat
        Message expectedMessage = new Message(sender, formattedTimestamp, content, chat);
        expectedMessage.setMs_id(1L); // Set a non-null ID for the message

        when(chatController.createAndOrGetChat(anyString())).thenReturn(chat);
        when(messageService.save(any(Message.class))).thenReturn(expectedMessage);
        when(chatService.save(any(Chat.class))).thenAnswer(invocation -> {
            Chat chatToSave = invocation.getArgument(0);
            chatToSave.setChatId(1L); // Set a valid chatId
            return chatToSave; // Return the chat object with the ID set
        });


        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch subscriptionLatch = new CountDownLatch(1);

        StompSession session = stompClient.connect("ws://localhost:" + port + "/chat", new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("WebSocket connected: " + connectedHeaders);
                session.subscribe("/topic/messages/" + toUser, new DefaultStompFrameHandler());
                subscriptionLatch.countDown();
                connectionLatch.countDown();
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("StompException: " + exception.getMessage());
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("TransportError: " + exception.getMessage());
            }
        }).get(15, TimeUnit.SECONDS);

        try {
            if (!connectionLatch.await(15, TimeUnit.SECONDS)) {
                fail("WebSocket connection failed");
            }
            if (!subscriptionLatch.await(15, TimeUnit.SECONDS)) {
                fail("Subscription failed");
            }

            System.out.println("Sending message to controller...");
            Thread.sleep(100); // Add a delay before sending
            chatController.sendMessage(toUser, new Message(sender, formattedTimestamp, content, chat));

            // Capture and verify sent message
            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(simpMessagingTemplate).convertAndSend(eq("/topic/messages/" + toUser), messageCaptor.capture());
            System.out.println("Verified message sent to template.");

            Message capturedMessage = messageCaptor.getValue();
            assertEquals(content, capturedMessage.getContent());
            assertEquals(sender, capturedMessage.getSender());

            Message receivedMessage = blockingQueue.poll(15, TimeUnit.SECONDS);
            if (receivedMessage != null) {
                System.out.println("Received message: " + receivedMessage);
                assertEquals(content, receivedMessage.getContent());
                assertEquals(sender, receivedMessage.getSender());
            } else {
                fail("Message not received within the timeout");
            }
        } finally {
            // Close the session after the test
            session.disconnect();
        }
    }

    class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Headers: " + headers.toString());
            System.out.println("Payload type: " + payload.getClass().getName());
            blockingQueue.add((Message) payload);
            System.out.println("Received frame in DefaultStompFrameHandler: " + payload);
        }
    }

    @Test
    public void testGetMessages() throws Exception {
        List<Message> messageList = new ArrayList<>();
        Message message = new Message();
        message.setSender("testUser");
        message.setContent("Hello");
        messageList.add(message);

        Chat chat = new Chat();
        chat.setName("testChat");

        when(chatService.findChatByName(anyString())).thenReturn(chat);
        when(messageService.findAllByChat(any(Chat.class))).thenReturn(messageList);

        ChatNameDTO chatNameDTO = new ChatNameDTO();
        chatNameDTO.setChat("testChat");

        mockMvc.perform(post("/getMessages")
                        .header("Authorization", "Bearer " + jwtToken)  // Add JWT token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(chatNameDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sender").value("testUser"))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    public void testGetChats() throws Exception {
        List<Chat> chatList = new ArrayList<>();
        Chat chat = new Chat();
        chat.setName("testChat");
        chatList.add(chat);

        when(chatService.findByParticipant(anyString())).thenReturn(chatList);

        mockMvc.perform(post("/getChats")
                        .header("Authorization", "Bearer " + jwtToken)  // Add your JWT token here
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString("testUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("testChat"));
    }
}
