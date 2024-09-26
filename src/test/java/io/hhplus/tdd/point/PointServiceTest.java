package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PointServiceTest {
    @Mock
    PointHistoryTable pointHistoryTable;

    @Mock
    UserPointTable userPointTable;

    PointService pointService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @DisplayName("충전/이용내역이 비어있을 경우")
    @Test
    void emptyHistory(){
        long userId = 1L;
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> pointService.selectHistoryList(userId));
        // 충잔/이용 내역이 비어있을 경우 Exception 발생하는지 테스트
    }

    @DisplayName("최대잔고를 벗어난 경우")
    @Test
    void pointPlusMax(){
        long userId = 1L;
        long amount = 550000;
        UserPoint userPoint = new UserPoint(userId, 550000, 1000);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        assertThrows(IllegalArgumentException.class, () -> pointService.pointCharge(userId, amount));
        // 충전할 금액으로 인해 최대 잔고금액을 벗어날 경우 Exception 발생하는지 테스트
    }

    @DisplayName("잔고가 부족할경우")
    @Test
    void pointMinusMin(){
        long userId = 1L;
        long amount = 560000;
        UserPoint userPoint = new UserPoint(userId, 550000, 1000);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        assertThrows(IllegalArgumentException.class, () -> pointService.pointUse(userId, amount));
        // 잔고가 부족할 경우 Exception 발생하는지 테스트
    }


}