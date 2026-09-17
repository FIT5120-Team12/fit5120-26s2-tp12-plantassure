<script setup lang="ts">
  import { computed, onBeforeUnmount, ref } from 'vue';
  import { useRouter } from 'vue-router';

  import { IDENTIFICATION_MAX_IMAGE_SIZE_BYTES, identifyPlant } from '@/api/identification';
  import identifyHeroBotanical from '@/assets/images/identify-hero-botanical.png';
  import IdentificationCandidateCard from '@/components/identification/IdentificationCandidateCard.vue';
  import IdentificationNotice from '@/components/identification/IdentificationNotice.vue';
  import IdentificationTips from '@/components/identification/IdentificationTips.vue';
  import PlantImageUploader from '@/components/identification/PlantImageUploader.vue';
  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import type { IdentificationCandidate } from '@/types/identification';

  const SUPPORTED_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

  const router = useRouter();
  const uploader = ref<InstanceType<typeof PlantImageUploader> | null>(null);
  const selectedImage = ref<File | null>(null);
  const previewUrl = ref<string | null>(null);
  const validationMessage = ref<string | null>(null);
  const isIdentifying = ref(false);
  const identificationError = ref<string | null>(null);
  const identificationCandidates = ref<IdentificationCandidate[]>([]);
  const hasIdentificationResult = ref(false);
  const identificationRequestId = ref(0);
  const imageMetadata = computed(() => {
    if (!selectedImage.value) return null;

    return `${selectedImage.value.type} · ${formatFileSize(selectedImage.value.size)}`;
  });

  function formatFileSize(size: number): string {
    if (size < 1024 * 1024) return `${Math.round(size / 1024)} KB`;

    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  function clearSelectedImage() {
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value);

    selectedImage.value = null;
    previewUrl.value = null;
    identificationRequestId.value += 1;
    isIdentifying.value = false;
    identificationError.value = null;
    identificationCandidates.value = [];
    hasIdentificationResult.value = false;
  }

  function selectImage(file: File) {
    if (!SUPPORTED_IMAGE_TYPES.has(file.type)) {
      validationMessage.value = 'Choose a JPEG, PNG, or WebP image.';
      return;
    }

    if (file.size > IDENTIFICATION_MAX_IMAGE_SIZE_BYTES) {
      validationMessage.value = 'Choose an image no larger than 10 MB.';
      return;
    }

    clearSelectedImage();
    selectedImage.value = file;
    previewUrl.value = URL.createObjectURL(file);
    validationMessage.value = null;
  }

  function replaceImage() {
    uploader.value?.openFileDialog();
  }

  function removeImage() {
    clearSelectedImage();
    validationMessage.value = null;
  }

  async function identifySelectedPlant() {
    const image = selectedImage.value;
    if (!image) return;

    const requestId = ++identificationRequestId.value;
    isIdentifying.value = true;
    identificationError.value = null;
    identificationCandidates.value = [];
    hasIdentificationResult.value = false;

    try {
      const response = await identifyPlant(image);
      if (requestId !== identificationRequestId.value) return;

      identificationCandidates.value = response.matches;
      hasIdentificationResult.value = true;
    } catch {
      if (requestId !== identificationRequestId.value) return;

      identificationError.value = 'We couldn’t identify this plant. Please try again.';
    } finally {
      if (requestId === identificationRequestId.value) {
        isIdentifying.value = false;
      }
    }
  }

  function handleCandidateConfirm(candidate: IdentificationCandidate) {
    const plantId = candidate.plantId;

    if (
      candidate.plantAssureMatch !== true ||
      typeof plantId !== 'number' ||
      !Number.isSafeInteger(plantId) ||
      plantId <= 0
    ) {
      return;
    }

    void router.push({
      name: 'plant-assessment',
      params: {
        plantId,
      },
    });
  }

  function goToSearch() {
    void router.push({ name: 'home', hash: '#plant-search-input' });
  }

  onBeforeUnmount(clearSelectedImage);
</script>

<template>
  <div class="identification-page">
    <AppHeader @check-plant="goToSearch" />

    <main>
      <section class="identification-hero" aria-labelledby="identification-title">
        <div class="app-container">
          <div class="identification-hero__content">
            <div class="identification-hero__copy">
              <p class="identification-eyebrow">IDENTIFY FROM PHOTO</p>
              <h1 id="identification-title">Identify a Plant</h1>
              <p>
                Upload a plant photo to identify possible matches before viewing verified
                PlantAssure assessment information.
              </p>
            </div>
          </div>
          <div class="identification-hero__visual">
            <v-img
              :src="identifyHeroBotanical"
              alt=""
              contain
              position="right bottom"
              aria-hidden="true"
            />
          </div>
        </div>
      </section>

      <section class="identification-content" aria-label="Plant photo upload">
        <div class="app-container identification-content__inner">
          <PlantImageUploader
            ref="uploader"
            :error-message="validationMessage"
            @select="selectImage"
          />

          <section v-if="selectedImage && previewUrl" class="identification-preview">
            <div class="identification-preview__intro">
              <h2>Preview your photo</h2>
              <p>Check that the plant is clearly visible before continuing.</p>
            </div>

            <v-sheet class="identification-preview__card" border rounded="md" color="surface">
              <div class="identification-preview__image">
                <v-img :src="previewUrl" :alt="`Preview of ${selectedImage.name}`" contain />
              </div>
              <div class="identification-preview__details">
                <h3>{{ selectedImage.name }}</h3>
                <p>{{ imageMetadata }}</p>
                <div class="identification-preview__actions">
                  <v-btn color="primary" variant="outlined" type="button" @click="replaceImage">
                    Replace photo
                  </v-btn>
                  <v-btn color="primary" variant="text" type="button" @click="removeImage">
                    Remove photo
                  </v-btn>
                  <v-btn
                    color="primary"
                    variant="flat"
                    type="button"
                    :disabled="!selectedImage || isIdentifying"
                    :loading="isIdentifying"
                    @click="identifySelectedPlant"
                  >
                    {{ isIdentifying ? 'Identifying…' : 'Identify Plant' }}
                  </v-btn>
                </div>
                <p v-if="isIdentifying" class="identification-preview__request-status" role="status">
                  Identifying your plant photo…
                </p>
                <p
                  v-else-if="identificationError"
                  class="identification-preview__request-error"
                  role="alert"
                >
                  {{ identificationError }}
                </p>
              </div>
            </v-sheet>
          </section>

          <section
            v-if="hasIdentificationResult && !isIdentifying && !identificationError"
            class="identification-results"
            aria-labelledby="identification-results-title"
          >
            <div class="identification-results__intro">
              <h2 id="identification-results-title">Possible matches</h2>
              <p>
                Identification confidence describes how strongly the uploaded image matches this
                species. It does not indicate environmental risk.
              </p>
            </div>

            <p v-if="identificationCandidates.length === 0" class="identification-results__empty">
              We couldn’t find a reliable plant match from this photo. Try another clear photo.
            </p>

            <div v-else class="identification-results__grid">
              <IdentificationCandidateCard
                v-for="(candidate, index) in identificationCandidates"
                :key="`${candidate.plantId ?? candidate.scientificName}-${index}`"
                :candidate="candidate"
                @confirm="handleCandidateConfirm"
              />
            </div>
          </section>

          <IdentificationTips />
          <IdentificationNotice />
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
  .identification-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: var(--color-canvas);
  }

  main {
    flex: 1;
  }

  .identification-hero {
    border-bottom: 1px solid var(--color-border);
  }

  .identification-hero > .app-container {
    --hero-grid-gutter: max(var(--space-xl), calc((100vw - var(--content-max-width)) / 2));

    width: 100%;
    max-width: none;
    min-height: 600px;
    display: grid;
    grid-template-columns:
      minmax(0, calc(56vw - var(--hero-grid-gutter)))
      minmax(0, 44vw);
    align-items: center;
    padding-left: var(--hero-grid-gutter);
  }

  .identification-hero__content {
    width: 100%;
    display: flex;
    align-items: center;
    padding: var(--space-3xl) var(--space-2xl) var(--space-3xl) 0;
  }

  .identification-hero__copy {
    width: 100%;
    max-width: 560px;
  }

  .identification-hero__visual {
    align-self: stretch;
    min-width: 0;
    padding-left: var(--space-lg);
  }

  .identification-hero__visual :deep(.v-img) {
    height: 100%;
  }

  .identification-eyebrow {
    margin: 0 0 var(--space-sm);
    color: var(--color-accent);
    font-size: 0.75rem;
    font-weight: 800;
    letter-spacing: 0.12em;
  }

  .identification-hero h1,
  .identification-preview h2,
  .identification-preview h3 {
    margin: 0;
    color: var(--color-primary);
  }

  .identification-hero h1 {
    max-width: 16ch;
    font-size: clamp(2.75rem, 6vw, 4.5rem);
    line-height: 0.98;
  }

  .identification-hero p:last-child {
    max-width: 42rem;
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    font-size: 1.0625rem;
  }

  .identification-content {
    padding-block: var(--space-2xl) var(--space-4xl);
  }

  .identification-content__inner {
    max-width: 52rem;
  }

  .identification-preview {
    margin-top: var(--space-2xl);
  }

  .identification-preview__intro {
    margin-bottom: var(--space-lg);
  }

  .identification-preview__intro p,
  .identification-preview__details > p {
    margin: var(--space-sm) 0 0;
    color: var(--color-ink-soft);
  }

  .identification-preview__card {
    display: grid;
    grid-template-columns: minmax(12rem, 1fr) minmax(0, 1fr);
    align-items: center;
    gap: var(--space-lg);
    padding: var(--space-md);
    border-color: var(--color-border);
  }

  .identification-preview__image {
    aspect-ratio: 4 / 3;
    overflow: hidden;
    border-radius: var(--radius-sm);
    background: var(--color-surface-muted);
  }

  .identification-preview__image :deep(.v-img) {
    height: 100%;
  }

  .identification-preview__details {
    min-width: 0;
  }

  .identification-preview__details h3 {
    overflow-wrap: anywhere;
    font-family: var(--font-body);
    font-size: 1.125rem;
    font-weight: 700;
  }

  .identification-preview__actions {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-sm);
    margin-top: var(--space-lg);
  }

  .identification-preview__request-error {
    margin: var(--space-md) 0 0;
    color: var(--color-error);
    font-size: 0.875rem;
  }

  .identification-preview__request-status {
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
  }

  .identification-results {
    margin-top: var(--space-3xl);
  }

  .identification-results__intro {
    max-width: 42rem;
  }

  .identification-results__intro h2 {
    margin: 0;
    color: var(--color-primary);
  }

  .identification-results__intro p,
  .identification-results__empty {
    margin: var(--space-sm) 0 0;
    color: var(--color-ink-soft);
  }

  .identification-results__empty {
    padding: var(--space-lg);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
  }

  .identification-results__grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--space-lg);
    margin-top: var(--space-xl);
  }

  @media (max-width: 1199px) {
    .identification-hero > .app-container {
      width: min(100% - 48px, var(--hero-max-width));
      max-width: var(--hero-max-width);
      min-height: 470px;
      grid-template-columns: minmax(0, 60fr) minmax(0, 40fr);
      padding-left: 0;
    }

    .identification-hero__content {
      padding: var(--space-2xl) var(--space-xl) var(--space-2xl) 0;
    }
  }

  @media (max-width: 899px) {
    .identification-hero > .app-container {
      min-height: auto;
      grid-template-columns: 1fr;
    }

    .identification-hero__content {
      padding: var(--space-2xl) 0 var(--space-xl);
    }

    .identification-hero__visual {
      padding-left: 0;
    }

    .identification-hero__visual :deep(.v-img) {
      height: auto;
    }
  }

  @media (max-width: 767px) {
    .identification-content {
      padding-block: var(--space-xl) var(--space-3xl);
    }

    .identification-hero > .app-container {
      grid-template-columns: 1fr;
    }

    .identification-results__grid {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 479px) {
    .identification-preview__card {
      grid-template-columns: 1fr;
    }
  }
</style>
