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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    @DisplayName("포인트 조회가 잘 되는 지 확인")
    @Test
    void selectPoint() {
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 100, 100);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        UserPoint result = pointService.selectPoint(userId);
        assertEquals(100, result.point());
        // 포인트 조회가 잘되는지 확인하는 테스트
    }


    @DisplayName("충전/이용내역이 있을 경우")
    @Test
    void historyList() {
        long userId = 1L;
        List<PointHistory> historyList = List.of(new PointHistory(1, userId, 500, TransactionType.CHARGE, 100));
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(historyList);

        List<PointHistory> result = pointService.selectHistoryList(userId);
        assertEquals(historyList.size(), 1);
        // 충전/이용 내역이 있을 경우 조회가 잘 되는지 확인
    }

    @DisplayName("충전/이용내역이 비어있을 경우")
    @Test
    void emptyHistory(){
        long userId = 1L;
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> pointService.selectHistoryList(userId));
        // 충잔/이용 내역이 비어있을 경우 Exception 발생하는지 테스트
    }

    @DisplayName("충전이 잘되는지 테스트")
    @Test
    void addPoint() {
        long userId = 1L;

        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 500, 1000));

        pointService.pointCharge(userId, 500);

        verify(pointHistoryTable).insert(eq(userId), eq(500L), eq(TransactionType.CHARGE), anyLong());
        verify(userPointTable).insertOrUpdate(userId, 500 + 500);
        // 충전이 잘 됐는지 / history에 잘 들어가는지 확인하는 테스트
    }

    @DisplayName("사용이 잘되는지 테스트")
    @Test
    void usePoint() {
        long userId = 1L;

        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 500, 1000));

        pointService.pointUse(userId, 500);

        verify(pointHistoryTable).insert(eq(userId), eq(500L), eq(TransactionType.USE), anyLong());
        verify(userPointTable).insertOrUpdate(userId, 500 - 500);
        // 사용이 잘 됐는지 / history에 잘 들어가는지 확인하는 테스트
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