package com.lucky.bonus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucky.bonus.dto.KafkaMsg;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickConsumer {

    @Value("${finopenapi.url}")
    private String financialApiUrl;

    @Value("${finopenapi.key}")
    private String financialApiKey;

    private final String TOPIC_NAME = "clickTopic";
    private final ClickRepository clickRepository;
    private final WebClient webClient;
    private final NotificationService notificationService;

    @Transactional
    @KafkaListener(topics = TOPIC_NAME, groupId = "group_1")
    public void listener(String msg) {
        log.info("컨슈머 들어옴.");

        ObjectMapper mapper = new ObjectMapper();
        try{
            KafkaMsg kafkaMsg = mapper.readValue(msg, KafkaMsg.class);
            log.info("Flag value: {}", kafkaMsg.flag());

            Click click = clickRepository.findByBonusIdAndAccountNo(kafkaMsg.bonusId(), kafkaMsg.accountNo())
                    .orElseGet(() -> clickRepository.save(
                            Click.builder()
                                    .bonusId(kafkaMsg.bonusId())
                                    .expoPushToken(kafkaMsg.expoPushToken())
                                    .accountNo(kafkaMsg.accountNo())
                                    .userKey(kafkaMsg.userKey())
                                    .build()
                    ));
            click.increaseClickCnt();
//            if (kafkaMsg.flag()) {
//                log.info("flag is true, processing...");
//                for (Click clicked : clickRepository.findAllByBonusId(kafkaMsg.bonusId())) {
//
//                    Map<String, Object> accountRequestBody = new HashMap<>();
//                    Map<String, String> headerMap = new HashMap<>();
//
//                    Date today = new Date();
//                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
//                    String formattedDate = formatter.format(today);
//
//                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmmss");
//                    String formattedTime = timeFormatter.format(today);
//
//                    headerMap.put("apiName", "updateDemandDepositAccountDeposit");
//                    headerMap.put("transmissionDate", formattedDate);
//                    headerMap.put("transmissionTime", formattedTime);
//                    headerMap.put("institutionCode", "00100");
//                    headerMap.put("fintechAppNo", "001");
//                    headerMap.put("apiServiceCode", "updateDemandDepositAccountDeposit");
//                    headerMap.put("institutionTransactionUniqueNo", formattedDate + formattedTime + "000000");
//                    headerMap.put("apiKey", financialApiKey);
//                    headerMap.put("userKey", clicked.getUserKey());
//
//                    accountRequestBody.put("Header", headerMap);
//                    accountRequestBody.put("accountNo", clicked.getAccountNo());
//                    accountRequestBody.put("transactionBalance", String.valueOf(clicked.getCnt()*100));
//                    accountRequestBody.put("transactionSummary", "빵뿌리기 보너스 입금");
//
//                    webClient.post()
//                            .uri(financialApiUrl + "/v1/edu/demandDeposit/updateDemandDepositAccountDeposit")
//                            .body(BodyInserters.fromValue(accountRequestBody))
//                            .retrieve()
//                            .bodyToMono(Map.class)
//                            .block();
//
//                    log.info(clicked.getAccountNo()+"송금완료");
//
////                    notificationService.sendNotification(
////                            clicked.getExpoPushToken(),
////                            clicked.getCnt()*100 + "원 입급",
////                            "빵뿌리기 이벤트 입금",
////                            "arbaguette://crew/authorized/banking/transaction"
////                    );
//
//
//                    Thread.sleep(1000);
//
//
//                }
//
//            }
        }catch (Exception e){
            log.error("Error in listener", e);
        }



    }
}
