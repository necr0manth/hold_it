package dev.dsai03.hold_it.content.client.particles.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ParticleTickerHolder<T> {
    private Predicate<T> predicateTicker;
    private Consumer<T> consumerTicker;
    private BiConsumer<T, Float> renderTicker;
    private Consumer<T> discardAction;
    private int mainTicker = -1;

    public ParticleTickerHolder() {
        this.predicateTicker = t -> true;
        this.consumerTicker = t -> {
        };
        this.renderTicker = (t, dt) -> {
        };
    }

    public ParticleTickerHolder<T> combine(ParticleTickerHolder<T> other) {
        if (mainTicker == 0) {
            var predicateTicker = asPredicateTicker();
            var otherPredicateTicker = other.asPredicateTicker();
            return new ParticleTickerHolder<>(t -> predicateTicker.test(t) || otherPredicateTicker.test(t));
        } else if (mainTicker == 1) {
            var consumerTicker = asConsumerTicker();
            var otherConsumerTicker = other.asConsumerTicker();
            return new ParticleTickerHolder<>(t -> {
                consumerTicker.accept(t);
                otherConsumerTicker.accept(t);
            });
        } else {
            var renderTicker = asRenderTicker();
            var otherRenderTicker = other.asRenderTicker();
            return new ParticleTickerHolder<>((t, dt) -> {
                renderTicker.accept(t, dt);
                otherRenderTicker.accept(t, dt);
            });
        }
    }

    public ParticleTickerHolder(Predicate<T> predicateTicker) {
        this.predicateTicker = t -> {
            if (discardAction == null)
                return predicateTicker.test(t);
            if (predicateTicker.test(t))
                discard(t);
            return true;
        };
        mainTicker = 0;
    }

    public ParticleTickerHolder(Consumer<T> consumerTicker) {
        this.consumerTicker = consumerTicker;
        mainTicker = 1;
    }

    public ParticleTickerHolder(BiConsumer<T, Float> renderTicker) {
        this.renderTicker = renderTicker;
        mainTicker = 2;
    }

    private void discard(T particle) {
        if (discardAction != null) {
            discardAction.accept(particle);
        }
    }

    public <V extends T> Predicate<V> asPredicateTicker() {
        if (predicateTicker != null)
            return (Predicate<V>) predicateTicker;
        if (consumerTicker != null)
            return (Predicate<V>) (predicateTicker = t -> {
                            consumerTicker.accept(t);
                            return true;
                        });
        return (Predicate<V>) (predicateTicker = t -> {
                    renderTicker.accept(t, 0.05f);
                    return true;
                });
    }

    public <V extends T> Consumer<V> asConsumerTicker() {
        if (consumerTicker != null)
            return (Consumer<V>) consumerTicker;
        if (predicateTicker != null)
            return (Consumer<V>) (consumerTicker = t -> {
                            if (predicateTicker.test(t)) discard(t);
                        });
        return (Consumer<V>) (consumerTicker = t -> renderTicker.accept(t, 0.05f));
    }

    public <V extends T> BiConsumer<V, Float> asRenderTicker() {
        if (renderTicker != null)
            return (BiConsumer<V, Float>) renderTicker;
        if (predicateTicker != null)
            return (BiConsumer<V, Float>) (renderTicker = (t, dt) -> {
                            if (predicateTicker.test(t)) discard(t);
                        });
        return (BiConsumer<V, Float>) (renderTicker = (t, dt) -> consumerTicker.accept(t));
    }

    public void setDiscardAction(Consumer<T> discardAction) {
        if (discardAction == null)
            throw new RuntimeException("Иди нахуй, так нельзя. Я тебе запрещаю o_0. (discardAction != null)");
        if (isRenderTicker())
            throw new RuntimeException("Иди нахуй, так нельзя. Я тебе запрещаю o_0. (isRenderTicker())");
        this.discardAction = discardAction;
    }

    public boolean isRenderTicker() {
        return mainTicker == 2;
    }

    public boolean isTickTicker() {
        return mainTicker != 2;
    }

    public boolean isPredicateTicker() {
        return mainTicker == 0;
    }
}
