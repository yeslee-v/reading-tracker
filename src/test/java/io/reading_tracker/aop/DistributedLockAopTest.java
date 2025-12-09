package io.reading_tracker.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.reading_tracker.annotation.DistributedLock;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DistributedLockAopTest {

  @Mock private RedissonClient redissonClient;

  @Mock private RLock rLock;

  @Mock private ProceedingJoinPoint joinPoint;

  @Mock private MethodSignature signature;

  @InjectMocks private DistributedLockAop distributedLockAop;

  @BeforeEach
  void setUp() {
    Thread.interrupted(); // 테스트 전 인터럽트 플래그가 true라면 default 상태(false)로 변경
  }

  @AfterEach
  void tearDown() {
    Thread.interrupted(); // 테스트 종료 후 인터럽트 플래그 false로 복구
  }

  @Test
  @DisplayName("락 획득 중 인터럽트가 발생하면 플래그를 복구하고 예외를 전파해야 한다")
  void interruptedExceptionTest() throws Throwable {
    // given Redisson이 락을 잡으려고 하면 인터럽트가 발생하도록 설정하고
    Method mockMethod = TestTarget.class.getMethod("targetMethod");

    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getMethod()).thenReturn(mockMethod);
    when(signature.getParameterNames()).thenReturn(new String[] {});
    when(joinPoint.getArgs()).thenReturn(new Object[] {});

    when(redissonClient.getLock(anyString())).thenReturn(rLock);

    when(rLock.tryLock(anyLong(), anyLong(), any()))
        .thenThrow(new InterruptedException("Redisson 락 획득 중 인터럽트 발생"));

    // when distributedLockAop을 실행하면
    Throwable thrown = catchThrowable(() -> distributedLockAop.lock(joinPoint));

    // then 인터럽트 플래그를 true로 복구하고 InterruptedException이 발생한다
    assertThat(thrown).isInstanceOf(IllegalStateException.class);
    assertThat(thrown.getCause()).isInstanceOf(InterruptedException.class);
    assertThat(thrown.getMessage()).contains("Redisson 락 획득 중 인터럽트 발생");

    assertThat(Thread.currentThread().isInterrupted()).isTrue();
  }

  static class TestTarget {
    @DistributedLock(key = "'test-key'")
    public void targetMethod() {}
  }
}
