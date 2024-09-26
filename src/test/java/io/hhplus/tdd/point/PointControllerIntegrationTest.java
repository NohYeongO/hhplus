package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointControllerIntegrationTest {

    @Autowired
    PointController pointController;

    @Autowired
    PointHistoryTable pointHistoryTable;

    @Autowired
    UserPointTable userPointTable;

    @Autowired
    PointService pointService;

    @DisplayName("동시성 테스트 / 통합테스트")
    @Test
    void chargeOrUse(){
        long userId = 1L;

        // 여러 개의 비동기 작업을 동시에 실행
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->{
                    pointController.charge(userId, 5000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.charge(userId, 4000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.use(userId, 3000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.charge(userId, 5000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.use(userId, 4000);
                }),CompletableFuture.runAsync(() ->{
                    pointController.use(userId, 3000);
                })
        ).join();

        // history에서 조회
        List<PointHistory> list = pointController.history(userId).getBody();
        long[] point = {5000, 4000, 3000, 5000, 4000, 3000};

        // 순서 비교
        for (int i = 0; i < list.size(); i++) {
            long actualPoint = list.get(i).amount();
            assertEquals(point[i], actualPoint);
        }
        // userId로 조회
        UserPoint userPoint = pointController.point(userId).getBody();

        // 연산 값 확인
        assertEquals(userPoint.point(), 5000 + 4000 - 3000 + 5000 - 4000 - 3000);
    }

}