package com.sparta.springresttemplateclient.Naver.Service;

import com.sparta.springresttemplateclient.Naver.Dto.ItemDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "NAVER API") // 로그를 출력하기 위한 Lombok 애너테이션, "NAVER API"라는 토픽을 사용
@Service // Spring의 서비스 클래스임을 나타내는 애너테이션, 비즈니스 로직을 처리하는 클래스임을 의미
public class NaverApiService {

    // RestTemplate 인스턴스, HTTP 요청을 보내기 위해 사용
    private final RestTemplate restTemplate;

    // 애플리케이션의 properties 파일에서 clientId 값을 주입받음
    @Value("${social.naver.params.clientId}")
    private String clientId;

    // 애플리케이션의 properties 파일에서 clientSecret 값을 주입받음
    @Value("${social.naver.params.clientSecret}")
    private String clientSecret;

    // RestTemplateBuilder를 이용해 RestTemplate을 초기화하는 생성자
    public NaverApiService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // Naver API를 통해 아이템을 검색하는 메서드
    public List<ItemDto> searchItems(String query) {
        // 요청할 URL을 만들기 위한 URI 구성
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com") // 기본 URL 설정
                .path("/v1/search/shop.json") // 특정 API 엔드포인트 지정
                .queryParam("display", 15) // 검색 결과로 15개의 아이템을 표시하도록 설정
                .queryParam("query", query) // 검색 쿼리 파라미터 추가
                .encode() // URI 인코딩
                .build() // URI 생성
                .toUri();
        log.info("uri = " + uri); // 생성된 URI를 로그로 출력

        // GET 요청을 만들기 위한 RequestEntity 생성
        RequestEntity<Void> requestEntity = RequestEntity
                .get(uri) // HTTP GET 메서드 설정
                .header("X-Naver-Client-Id", clientId) // 요청 헤더에 Client ID 추가
                .header("X-Naver-Client-Secret", clientSecret) // 요청 헤더에 Client Secret 추가
                .build(); // RequestEntity 빌드

        // Naver API에 요청을 보내고, 응답을 ResponseEntity로 받음
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        // 응답 상태 코드를 로그로 출력
        log.info("NAVER API Status Code : " + responseEntity.getStatusCode());

        // 응답 바디(JSON 문자열)를 ItemDto 리스트로 변환하여 반환
        return fromJSONtoItems(responseEntity.getBody());
    }

    // JSON 응답을 ItemDto 객체의 리스트로 변환하는 메서드
    public List<ItemDto> fromJSONtoItems(String responseEntity) {
        JSONObject jsonObject = new JSONObject(responseEntity); // JSON 응답을 JSONObject로 변환
        JSONArray items  = jsonObject.getJSONArray("items"); // "items" 배열을 추출
        List<ItemDto> itemDtoList = new ArrayList<>(); // ItemDto 객체 리스트 초기화

        // JSONArray의 각 아이템을 ItemDto 객체로 변환하여 리스트에 추가
        for (Object item : items) {
            ItemDto itemDto = new ItemDto((JSONObject) item);
            itemDtoList.add(itemDto);
        }

        // 변환된 ItemDto 리스트 반환
        return itemDtoList;
    }
}