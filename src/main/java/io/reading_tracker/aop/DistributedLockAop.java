package io.reading_tracker.aop;

import io.reading_tracker.annotation.DistributedLock;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class DistributedLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";
  private final RedissonClient redissonClient;

  @Around("@annotation(io.reading_tracker.annotation.DistributedLock)")
  public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

    String key =
        REDISSON_LOCK_PREFIX
            + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

    RLock rLock = redissonClient.getLock(key);

    boolean isLocked = tryToLock(rLock, distributedLock, key);

    try {
      return joinPoint.proceed();
    } finally {
      if (isLocked) {
        safeUnlock(rLock, key);
      }
    }
  }

  private boolean tryToLock(RLock rLock, DistributedLock distributedLock, String key) {
    try {
      boolean available =
          rLock.tryLock(
              distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUtil());

      if (!available) {
        log.warn("락 획득 실패 - key: {}", key);
        throw new IllegalArgumentException("현재 처리 중인 요청으로 잠시 후 다시 시도하세요");
      }

      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Redisson 락 획득 중 인터럽트 발생: ", e);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      log.error("Redis 분산락 획득 실패. key: {}, error: {}", key, e.getMessage());

      return false;
    }
  }

  private void safeUnlock(RLock rLock, String key) {
    try {
      if (rLock.isHeldByCurrentThread()) {
        rLock.unlock();
      }
    } catch (IllegalMonitorStateException e) {
      log.info("락이 이미 해제되었습니다. key: {}", key);
    } catch (Exception e) {
      log.warn("락 해제 중 오류가 발생했습니다. key: {}, error: {}", key, e.getMessage());
    }
  }
}

class CustomSpringELParser {
  public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
    ExpressionParser parser = new SpelExpressionParser();
    EvaluationContext context = new StandardEvaluationContext();

    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], args[i]);
    }

    return parser.parseExpression(key).getValue(context, Object.class);
  }
}
