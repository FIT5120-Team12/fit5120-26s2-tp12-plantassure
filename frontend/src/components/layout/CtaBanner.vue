<script setup lang="ts">
  import decorationUrl from '@/assets/images/cta-botanical-decoration.png';

  withDefaults(
    defineProps<{
      eyebrow?: string;
      title: string;
      description: string;
      actionLabel?: string;
      compact?: boolean;
    }>(),
    {
      eyebrow: 'GET STARTED',
      actionLabel: 'Check a Plant',
      compact: false,
    },
  );

  defineEmits<{
    action: [];
  }>();
</script>

<template>
  <section
    class="cta-banner"
    :class="{ 'cta-banner--compact': compact }"
    aria-labelledby="cta-title"
  >
    <img class="cta-banner__decoration cta-banner__decoration--left" :src="decorationUrl" alt="" />
    <div class="cta-banner__content">
      <p class="cta-banner__eyebrow">{{ eyebrow }}</p>
      <h2 id="cta-title">{{ title }}</h2>
      <p class="cta-banner__description">{{ description }}</p>
      <button type="button" @click="$emit('action')">
        <span>{{ actionLabel }}</span>
        <i class="mdi mdi-arrow-right" aria-hidden="true"></i>
      </button>
    </div>
    <img class="cta-banner__decoration cta-banner__decoration--right" :src="decorationUrl" alt="" />
  </section>
</template>

<style scoped>
  .cta-banner {
    position: relative;
    min-height: 416px;
    display: grid;
    place-items: center;
    overflow: hidden;
    padding: 60px var(--space-2xl);
    border-radius: var(--radius-lg);
    background: var(--color-primary);
    color: var(--color-on-primary);
  }

  .cta-banner__content {
    min-width: 0;
    position: relative;
    z-index: 1;
    max-width: 440px;
    text-align: center;
  }

  .cta-banner--compact {
    min-height: 320px;
    padding-block: var(--space-2xl);
  }

  .cta-banner__eyebrow {
    margin: 0 0 var(--space-md);
    color: var(--color-accent-soft);
    font-size: 0.8125rem;
    font-weight: 700;
    letter-spacing: 0.14em;
  }

  .cta-banner h2 {
    margin: 0;
    color: var(--color-on-primary);
    font-size: 2.25rem;
    line-height: 1.08;
    overflow-wrap: anywhere;
  }

  .cta-banner__description {
    margin: 20px auto 36px;
    color: color-mix(in srgb, var(--color-on-primary) 78%, transparent);
    font-size: 1rem;
    overflow-wrap: anywhere;
  }

  .cta-banner button {
    min-height: 48px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: var(--space-xs);
    padding: 0 var(--space-lg);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
    color: var(--color-primary);
    font-weight: 600;
    cursor: pointer;
    transition: background-color 160ms ease;
  }

  .cta-banner button:hover {
    background: var(--color-surface-muted);
  }

  .cta-banner__decoration {
    position: absolute;
    bottom: 0;
    width: 32%;
    height: 100%;
    object-fit: cover;
    opacity: 0.9;
    -webkit-mask-image: linear-gradient(
      90deg,
      transparent 0%,
      #000 22%,
      #000 78%,
      transparent 100%
    );

    mask-image: linear-gradient(90deg, transparent 0%, #000 22%, #000 78%, transparent 100%);
    pointer-events: none;
  }

  .cta-banner__decoration--left {
    left: 0;
  }

  .cta-banner__decoration--right {
    right: 0;
    transform: scaleX(-1);
  }

  @media (max-width: 999px) {
    .cta-banner {
      min-height: 380px;
      padding: var(--space-2xl) 120px;
    }

    .cta-banner__decoration {
      width: 35%;
      height: 100%;
      bottom: 0;
      opacity: 0.45;
    }

    .cta-banner--compact {
      min-height: 320px;
    }
  }

  @media (max-width: 899px) {
    .cta-banner {
      padding-inline: 80px;
    }

    .cta-banner__decoration {
      width: 38%;
      opacity: 0.35;
    }
  }

  @media (max-width: 767px) {
    .cta-banner {
      min-height: 390px;
      padding: var(--space-2xl) var(--space-lg);
    }

    .cta-banner__content {
      max-width: 440px;
    }

    .cta-banner__decoration {
      width: 52%;
      height: 100%;
      bottom: 0;
      opacity: 0.25;
      -webkit-mask-image: none;
      mask-image: none;
    }

    .cta-banner__decoration--left {
      left: -22%;
    }

    .cta-banner__decoration--right {
      right: -22%;
    }

    .cta-banner--compact {
      min-height: 340px;
    }

    .cta-banner--compact .cta-banner__decoration {
      opacity: 0.18;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .cta-banner button {
      transition: none;
    }
  }
</style>
