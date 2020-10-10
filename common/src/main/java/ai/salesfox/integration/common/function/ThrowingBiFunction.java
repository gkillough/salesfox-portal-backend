package ai.salesfox.integration.common.function;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Throwable> {
    R apply(T t, U u) throws E;

}
