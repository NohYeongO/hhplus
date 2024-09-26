package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class PointControllerTest {

    @Mock
    PointService pointService;

    @DisplayName("충전금액이 최대잔고 금액보다 클 경우")
    @Test
    void maxPoint(){
        PointController pointController = new PointController(pointService);
        assertThrows(IllegalArgumentException.class, () -> {
            pointController.charge(1, 1000001);
        });
        // 요청 본문에 포함되어있는 충전금액이 최대잔고 금액보다 클경우 IllegalArgumentException 발생하는지 검증
    }

    @DisplayName("시용금액이 최대잔고 금액보다 클 경우")
    @Test
    void minPoint(){
        PointController sut = new PointController(pointService);
        assertThrows(IllegalArgumentException.class, () -> {
            sut.use(1, 1000001);
        });
        // 요청 본문에 포함되어있는 사용금액이 최대잔고 금액보다 클경우 IllegalArgumentException 발생하는지 검증
    }

    @DisplayName("충전금액이 음수일 경우")
    @Test
    void chargeNegativePoint(){
        PointController sut = new PointController(pointService);
        assertThrows(IllegalArgumentException.class, () -> {
            sut.charge(0, -1);
        });
        // 요청 본문에 포함되어있는 충전금액이 음수 일경우 IllegalArgumentException 발생하는지 검증
    }

    @DisplayName("사용금액이 음수일 경우")
    @Test
    void useNegativePoint(){
        PointController sut = new PointController(pointService);
        assertThrows(IllegalArgumentException.class, () -> {
            sut.use(0, -1);
        });
        // 요청 본문에 포함되어있는 사용금액이 음수 일경우 IllegalArgumentException 발생하는지 검증
    }

}