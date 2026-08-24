<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import memorySheetUrl from '@/assets/images/timeCapsules/open/memory-sheet.png'
import memory202401Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-01.png'
import memory202402Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-02.png'
import memory202403Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-03.png'
import memory202404Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-04.png'
import memory202405Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-05.png'
import memory202406Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-06.png'
import memory202407Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-07.png'
import memory202408Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-08.png'
import memory202409Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-09.png'
import memory202410Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-10.png'
import memory202411Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-11.png'
import memory202412Url from '@/assets/images/timeCapsules/open/memory-2024/memory-2024-12.png'
import { api } from '@/api'

type Memory = {
  year: number
  month: number
  title: string
  short: string
  letter: string
  photoUrl?: string
}

const allMemories = ref<Memory[]>([])
const years = computed(() => [...new Set(allMemories.value.map(({ year }) => year))].sort())
const emptyMemory: Memory = { year: new Date().getFullYear(), month: 1, title: '타임캡슐', short: '', letter: '' }
const OPEN_FLASH_STORAGE_KEY = 'azas_time_capsule_open_flash'
const shortMessages = [
  '오늘의 웃음을 오래 기억할게',
  '너와 함께라서 평범한 날도 특별했어',
  '천천히, 너의 속도로 자라렴',
] as const
const elementaryShortMessages = [
  '혼자서도 반짝이는 네 하루를 기억할게',
  '조금 더 넓어진 세상에서도 우리는 네 편이야',
  '네가 고른 꿈을 천천히 응원할게',
] as const
const memory2024PhotoUrls = [
  memory202401Url,
  memory202402Url,
  memory202403Url,
  memory202404Url,
  memory202405Url,
  memory202406Url,
  memory202407Url,
  memory202408Url,
  memory202409Url,
  memory202410Url,
  memory202411Url,
  memory202412Url,
] as const
const memoryTitlesByYear: Record<number, string[]> = {
  2023: [
    '처음 마주 본 봄',
    '작은 손의 온기',
    '함께 웃던 오후',
    '벚꽃 아래 우리',
    '한 뼘 더 자란 날',
    '햇살 가득한 소풍',
    '첫 여름의 바다',
    '달콤했던 생일',
    '가을빛 산책',
    '우리만의 여행',
    '포근한 집의 밤',
    '눈처럼 쌓인 사랑',
  ],
  2024: [
    '처음 고른 파란 저금통',
    '함께 적은 작은 약속',
    '손끝에 남은 응원',
    '기다림을 배운 봄',
    '조금씩 모인 마음',
    '웃음이 번진 주말',
    '우리의 첫 계획표',
    '네 이름으로 남긴 기록',
    '가족이 모인 저녁',
    '가을 햇살 속 다짐',
    '오래 남을 칭찬',
    '따뜻하게 닫은 한 해',
  ],
  2025: [
    '다시 꺼낸 작은 꿈',
    '어제보다 넓어진 세상',
    '함께 기다린 선물',
    '새봄에 남긴 편지',
    '용기를 배운 오후',
    '네가 고른 첫 목표',
    '반짝이던 여름 기록',
    '생일처럼 환한 날',
    '걷다 발견한 마음',
    '우리만 아는 여행',
    '포근한 응원의 밤',
    '다음 해로 건넨 사랑',
  ],
}
const getMemoryShort = (year: number, index: number) => {
  const messages = year === 2024 ? elementaryShortMessages : shortMessages
  return messages[index % messages.length] ?? messages[0]
}

const getMemoryLetter = (year: number, month: number, title: string) => {
  if (year === 2024) {
    return `사랑하는 우리 아이에게.\n\n${year}년 ${month}월, ${title}을 오래 기억하고 싶어. 어느새 가방을 메고 네 하루를 스스로 걸어가는 모습을 보며 엄마, 아빠는 매일 새롭게 감동하고 있어.\n\n서툰 날도 있고 자신 있는 날도 있겠지만 괜찮아. 네가 배운 작은 용기와 웃음을 이곳에 담아둘게. 언제든 돌아보면 우리가 얼마나 너를 믿고 응원했는지 느낄 수 있기를 바라.`
  }

  return `사랑하는 우리 아가에게.\n\n${year}년 ${month}월, ${title}을 기억하니? 작은 순간 하나도 놓치고 싶지 않아 사진과 마음을 이곳에 함께 담아두었어.\n\n앞으로 네가 어떤 꿈을 만나더라도 지금처럼 환하게 웃기를 바라. 서두르지 않아도 괜찮아. 우리는 언제나 네 곁에서 같은 마음으로 응원할게.`
}

const localTimeCapsuleMemories: Memory[] = Object.entries(memoryTitlesByYear).flatMap(
  ([year, titles]) =>
    titles.map((title, index) => {
      const memoryYear = Number(year)
      const month = index + 1

      return {
        year: memoryYear,
        month,
        title,
        short: getMemoryShort(memoryYear, index),
        letter: getMemoryLetter(memoryYear, month, title),
      }
    }),
)

const currentIndex = ref(0)
const selectedMemoryIndex = ref<number | null>(null)
const activeYear = ref(new Date().getFullYear())
const isLetterOpen = ref(false)
const isFullLetterModalOpen = ref(false)
const isOverviewModalOpen = ref(false)
const isCollageSaved = ref(false)
const isOpeningFlashVisible = ref(false)
const touchStartX = ref<number | null>(null)

const currentMemory = computed<Memory>(() => allMemories.value[currentIndex.value] ?? emptyMemory)
const selectedMemory = computed<Memory>(() =>
  selectedMemoryIndex.value === null
    ? currentMemory.value
    : (allMemories.value[selectedMemoryIndex.value] ?? currentMemory.value),
)
const currentMemoryLabel = computed(() => String(currentIndex.value + 1).padStart(2, '0'))
const savingDurationMonths = computed(() => allMemories.value.length)
const savingDurationYears = computed(() => Math.max(1, Math.round(savingDurationMonths.value / 12)))
const isFirstMemory = computed(() => currentIndex.value === 0)
const isLastMemory = computed(() => currentIndex.value === allMemories.value.length - 1)
const activeYearMemories = computed(() =>
  allMemories.value
    .map((memory, index) => ({ ...memory, index }))
    .filter((memory) => memory.year === activeYear.value),
)
const galleryRows = computed(() => {
  const items: Array<{
    kind: 'photo' | 'note'
    memory: Memory
    index: number
    monthIndex: number
  }> = []

  activeYearMemories.value.forEach((memory, monthIndex) => {
    items.push({ kind: 'photo', memory, index: memory.index, monthIndex })
    if (monthIndex === 2 || monthIndex === 6 || monthIndex === 10) {
      items.push({ kind: 'note', memory, index: memory.index, monthIndex })
    }
  })

  return Array.from({ length: 5 }, (_, index) => items.slice(index * 3, index * 3 + 3))
})

const getPhotoStyle = (index: number) => {
  const memory = allMemories.value[index]
  if (memory?.photoUrl) {
    return {
      backgroundImage: `url(${memory.photoUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }

  if (memory?.year === 2024) {
    const photoUrl = memory2024PhotoUrls[(memory.month - 1) % memory2024PhotoUrls.length]

    return {
      backgroundImage: `url(${photoUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }

  const column = index % 3
  const row = Math.floor(index / 3) % 4

  return {
    backgroundImage: `url(${memorySheetUrl})`,
    backgroundSize: '300% 400%',
    backgroundPosition: `${column * 50}% ${row * (100 / 3)}%`,
  }
}

const moveMemory = (direction: number) => {
  const nextIndex = Math.min(Math.max(currentIndex.value + direction, 0), Math.max(allMemories.value.length - 1, 0))

  if (nextIndex === currentIndex.value) return

  currentIndex.value = nextIndex
  isLetterOpen.value = false
}

const handleTouchStart = (event: TouchEvent) => {
  touchStartX.value = event.touches[0]?.clientX ?? null
}

const handleTouchEnd = (event: TouchEvent) => {
  if (touchStartX.value === null) return

  const endX = event.changedTouches[0]?.clientX ?? touchStartX.value
  const distance = endX - touchStartX.value

  if (distance < -55) moveMemory(1)
  if (distance > 55) moveMemory(-1)

  touchStartX.value = null
}

const openMemoryFromOverview = (index: number) => {
  currentIndex.value = index
  isOverviewModalOpen.value = false
  isCollageSaved.value = false
  isLetterOpen.value = false
}

const openLetterModal = (index: number) => {
  selectedMemoryIndex.value = index
  isFullLetterModalOpen.value = true
}

const openOverviewModal = () => {
  isCollageSaved.value = false
  isOverviewModalOpen.value = true
}

const closeOverviewModal = () => {
  isOverviewModalOpen.value = false
  isCollageSaved.value = false
}

const saveOverviewScene = () => {
  isCollageSaved.value = true
}

const router = useRouter()
const route = useRoute()
const goBack = () => router.back()
const routeCapsuleListId = computed(() => String(route.params.capsuleListId ?? 'local'))
const isLocalTimeCapsuleRoute = computed(
  () => route.name === 'LocalTimeCapsuleOpen' || routeCapsuleListId.value === 'local',
)
const goToList = () => router.push(`/time-capsules/${routeCapsuleListId.value}`)

const applyLocalTimeCapsuleFallback = () => {
  allMemories.value = localTimeCapsuleMemories
  activeYear.value = localTimeCapsuleMemories[0]?.year ?? new Date().getFullYear()
}

const getEntryDate = (contributedAt?: string) => {
  const date = contributedAt ? new Date(contributedAt) : new Date()
  return Number.isNaN(date.getTime()) ? new Date() : date
}

const createEntryLetter = (year: number, month: number, title: string, amount?: number) => {
  const amountText =
    amount && amount > 0 ? `\n\n이날 함께 남긴 금액은 ${amount.toLocaleString('ko-KR')}원이야.` : ''

  return `사랑하는 우리 아이에게.\n\n${year}년 ${month}월, ${title}을 이 타임캡슐에 담아두었어.${amountText}\n\n시간이 지나 다시 꺼내보는 오늘, 이 순간의 마음과 기록이 오래 따뜻하게 남기를 바라.`
}

onMounted(async () => {
  if (window.sessionStorage.getItem(OPEN_FLASH_STORAGE_KEY) === 'true') {
    isOpeningFlashVisible.value = true
    window.sessionStorage.removeItem(OPEN_FLASH_STORAGE_KEY)
    window.setTimeout(() => {
      isOpeningFlashVisible.value = false
    }, 160)
  }

  try {
    if (isLocalTimeCapsuleRoute.value) {
      applyLocalTimeCapsuleFallback()
      return
    }

    const capsuleListId = Number(route.params.capsuleListId)
    if (!Number.isFinite(capsuleListId)) {
      return
    }

    const { data } = await api.getTimeCapsuleEntriesUsingGET(capsuleListId)

    allMemories.value = (data.entries ?? []).map((entry) => {
      const date = getEntryDate(entry.contributed_at)
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const title = entry.title ?? '소중한 기록'

      return {
        year,
        month,
        title,
        short: title,
        letter: createEntryLetter(year, month, title, entry.contribution_amount),
        photoUrl: entry.thumbnail_url,
      }
    })
    activeYear.value = years.value[0] ?? new Date().getFullYear()
  } catch (error) {
    if (isLocalTimeCapsuleRoute.value) applyLocalTimeCapsuleFallback()
  }
})
</script>

<template>
  <main class="time-capsule-open">
    <Transition name="open-flash">
      <div v-if="isOpeningFlashVisible" class="open-white-flash" aria-hidden="true"></div>
    </Transition>

    <div class="ambient ambient-one" aria-hidden="true"></div>
    <div class="ambient ambient-two" aria-hidden="true"></div>

    <section
      class="first-memory"
      aria-label="타임캡슐 추억 기록"
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
    >
      <div class="first-memory-copy">
        <p class="eyebrow"><span></span> 우리 아이 타임캡슐 <span></span></p>
        <p class="ago-copy">
          {{
            isFirstMemory
              ? `${savingDurationYears}년 전, 우리가 처음 남긴 이야기`
              : `${currentIndex + 1}번째로 꺼낸 우리의 이야기`
          }}
        </p>
        <h1>{{ currentMemory.title }}</h1>
        <p class="memory-count">
          MEMORY {{ currentMemoryLabel }} <i></i> {{ savingDurationMonths }}
        </p>
      </div>

      <div class="first-keepsake">
        <article class="first-polaroid">
          <i class="first-clip" aria-hidden="true"></i>
          <div
            class="memory-photo"
            :style="getPhotoStyle(currentIndex)"
            role="img"
            :aria-label="`${currentMemory.year}년 ${currentMemory.month}월 ${currentMemory.title}`"
          ></div>
          <div class="first-polaroid-caption">
            <span>{{ currentMemory.year }}. {{ String(currentMemory.month).padStart(2, '0') }}</span>
            <b>{{ isFirstMemory ? '우리의 첫 번째 기록' : currentMemory.short }}</b>
          </div>
        </article>

        <article class="first-letter" :class="{ open: isLetterOpen }">
          <div class="letter-tape" aria-hidden="true"></div>
          <p class="letter-to">To. 사랑하는 우리 아가에게</p>
          <p class="letter-body">{{ currentMemory.letter }}</p>
          <div v-if="!isLetterOpen" class="first-letter-fade" aria-hidden="true"></div>
          <button class="first-letter-more" type="button" @click="openLetterModal(currentIndex)">
            편지 전체 읽기
          </button>
        </article>
      </div>

      <div class="journey-actions">
        <button
          class="journey-prev"
          type="button"
          :disabled="isFirstMemory"
          aria-label="이전 기록"
          @click="moveMemory(-1)"
        >
          이전 추억 보기
        </button>
        <button
          class="journey-next"
          type="button"
          @click="isLastMemory ? openOverviewModal() : moveMemory(1)"
        >
          {{ isLastMemory ? '마지막 추억 콜라주 보기' : '다음 추억 보기' }}
        </button>
      </div>
    </section>

    <section class="album-section" aria-label="타임캡슐 추억 전시">
      <div class="section-copy">
        <p class="tiny-title">OUR LITTLE MOMENTS</p>
        <h2>시간을 걸어둔 작은 전시</h2>
        <p>연도를 고르고, 마음이 머무는 사진을 눌러보세요.</p>
      </div>

      <div class="year-tabs" role="tablist" aria-label="연도 선택">
        <button
          v-for="year in years"
          :key="year"
          class="year-tab"
          :class="{ active: activeYear === year }"
          type="button"
          role="tab"
          :aria-selected="activeYear === year"
          @click="activeYear = year"
        >
          <strong>{{ year }}</strong>
          <span>{{ activeYear === year ? '우리의 기록' : '열어보기' }}</span>
        </button>
      </div>

      <div class="gallery-frame">
        <div class="frame-inner">
          <div class="frame-label"><span>{{ activeYear }}</span> · THE YEAR WE LOVED</div>
          <div class="hanging-gallery" aria-label="연도별 추억 사진">
            <div v-for="(row, rowIndex) in galleryRows" :key="rowIndex" class="memory-row">
              <div class="rope" aria-hidden="true"></div>

              <button
                v-for="(item, itemIndex) in row"
                :key="`${item.kind}-${item.index}`"
                class="gallery-item"
                :class="[
                  item.kind === 'photo' ? 'polaroid' : `note-card note-${(item.monthIndex % 3) + 1}`,
                ]"
                :style="{
                  transform: `rotate(${(((rowIndex * 3 + itemIndex) % 5) - 2) * 1.2}deg)`,
                }"
                type="button"
                :aria-label="`${item.memory.year}년 ${item.memory.month}월 ${item.memory.title} 보기`"
                @click="openLetterModal(item.index)"
              >
                <i class="clip" aria-hidden="true"></i>

                <template v-if="item.kind === 'photo'">
                  <span class="memory-photo" :style="getPhotoStyle(item.index)"></span>
                  <span class="polaroid-month">{{ item.memory.month }}월</span>
                </template>

                <template v-else>
                  <b>To. 사랑하는 너에게</b>
                  <span>{{ item.memory.short }}</span>
                  <small>엄마, 아빠가</small>
                </template>
              </button>
            </div>
          </div>
          <div class="frame-footer">
            <span>사진 12장</span><i>·</i><span>편지 12통</span><i>·</i
            ><span>{{ years.indexOf(activeYear) + 1 }}년 차</span>
          </div>
        </div>
      </div>

      <p class="album-caption">
        하나씩 꺼내보면, 평범했던 날들이<br />얼마나 눈부셨는지 알게 됩니다.
      </p>
    </section>

    <section class="ending" aria-label="타임캡슐 마무리">
      <div class="heart-line"><span>✦</span></div>
      <p>{{ savingDurationMonths }}개월의 마음을 한 장에</p>
      <h2>우리의 시간이<br />하나의 작품이 되었어요</h2>
      <button class="collage-button" type="button" @click="openOverviewModal">
        마지막 추억 콜라주 보기
      </button>

      <button class="list-button" type="button" @click="goToList">기록 리스트 보기</button>
      <button class="back-button" type="button" @click="goBack">돌아가기</button>
    </section>

    <Teleport to="body">
      <div
        v-if="isFullLetterModalOpen"
        class="modal-backdrop"
        role="presentation"
        @mousedown="isFullLetterModalOpen = false"
      >
        <section
          class="memory-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="full-letter-title"
          @mousedown.stop
        >
          <button
            class="modal-close"
            type="button"
            aria-label="닫기"
            @click="isFullLetterModalOpen = false"
          >
            ×
          </button>
          <div class="modal-photo-wrap">
            <div
              class="memory-photo"
              :style="getPhotoStyle(selectedMemoryIndex ?? currentIndex)"
            ></div>
            <span class="modal-date">
              {{ selectedMemory.year }}. {{ String(selectedMemory.month).padStart(2, '0') }}
            </span>
          </div>
          <div class="modal-body">
            <p class="modal-kicker">
              MEMORY {{ String((selectedMemoryIndex ?? currentIndex) + 1).padStart(2, '0') }} /
              {{ savingDurationMonths }}
            </p>
            <h2 id="full-letter-title">{{ selectedMemory.title }}</h2>
            <p class="modal-short">{{ selectedMemory.short }}</p>
            <div class="letter-preview open">
              <b>To. 사랑하는 우리 아가에게</b>
              <p>{{ selectedMemory.letter }}</p>
            </div>
            <button
              class="letter-toggle"
              type="button"
              @click="isFullLetterModalOpen = false"
            >
              편지 닫기
            </button>
          </div>
        </section>
      </div>

      <div
        v-if="isOverviewModalOpen"
        class="modal-backdrop collage-backdrop"
        role="presentation"
        @mousedown="closeOverviewModal"
      >
        <section
          class="collage-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="overview-title"
          @mousedown.stop
        >
          <button
            class="modal-close"
            type="button"
            aria-label="닫기"
            @click="closeOverviewModal"
          >
            ×
          </button>
          <p class="tiny-title">{{ savingDurationMonths }} MONTHS, ONE LOVE</p>
          <h2 id="overview-title">함께 자란 우리의 시간</h2>
          <p class="collage-desc">각 계절에서 한 장씩 고른 12개의 마음</p>

          <div class="final-collage">
            <button
              v-for="(_, index) in 12"
              :key="index"
              class="collage-photo-button"
              type="button"
              :aria-label="`${index + 1}번째 대표 추억 보기`"
              @click="openMemoryFromOverview(index * 3)"
            >
              <span class="memory-photo" :style="getPhotoStyle(index * 3)"></span>
            </button>
          </div>

          <button class="save-button" type="button" @click="saveOverviewScene">
            {{ isCollageSaved ? '저장되었습니다' : '이 장면 간직하기' }}
          </button>
        </section>
      </div>
    </Teleport>
  </main>
</template>

<style scoped>
.time-capsule-open {
  position: relative;
  min-height: 100dvh;
  overflow: hidden;
  color: #29343a;
  background: #f9fbfb;
  font-family:
    Pretendard,
    'Noto Sans KR',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
}

.time-capsule-open button {
  font: inherit;
}

.open-white-flash {
  position: fixed;
  inset: 0;
  z-index: 100;
  pointer-events: none;
  background: #fff;
}

.open-flash-leave-active {
  transition: opacity 1100ms ease;
}

.open-flash-leave-to {
  opacity: 0;
}

.ambient {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  opacity: 0.48;
}

.ambient-one {
  top: -120px;
  right: -100px;
  width: 320px;
  height: 320px;
  background: #d9effc;
}

.ambient-two {
  bottom: 5%;
  left: -160px;
  width: 280px;
  height: 280px;
  background: #fff0c9;
}

.first-memory {
  position: relative;
  min-height: 100dvh;
  padding: 64px 20px 46px;
  background:
    radial-gradient(circle at 12% 12%, rgba(255, 255, 255, 0.96), transparent 26%),
    radial-gradient(circle at 92% 34%, rgba(246, 226, 170, 0.25), transparent 24%),
    linear-gradient(180deg, #eaf7ff 0%, #f8fbfa 43%, #fffaf0 100%);
}

.first-memory::before,
.first-memory::after {
  position: absolute;
  width: 54px;
  height: 26px;
  border-top: 1px solid rgba(111, 165, 195, 0.32);
  border-radius: 50%;
  pointer-events: none;
  content: '';
}

.first-memory::before {
  top: 12%;
  left: -8px;
  transform: rotate(-18deg);
}

.first-memory::after {
  top: 32%;
  right: -14px;
  transform: rotate(18deg);
}

.first-memory-copy {
  width: min(100%, 420px);
  margin: 0 auto 28px;
  text-align: center;
}

.eyebrow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: #76a9c8;
}

.eyebrow span {
  width: 26px;
  height: 1px;
  background: #a9d2e9;
}

.ago-copy {
  margin: 24px 0 8px;
  color: #7699aa;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.6;
  letter-spacing: -0.02em;
}

.first-memory-copy h1 {
  margin: 0;
  color: #33434a;
  font: 400 31px/1.4 Georgia, 'Noto Serif KR', serif;
  letter-spacing: -0.045em;
}

.memory-count {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  margin: 11px 0 0;
  color: #9dafb6;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.memory-count i {
  width: 26px;
  height: 1px;
  background: #c5d5da;
}

.first-keepsake {
  position: relative;
  width: min(100%, 380px);
  margin: 0 auto;
}

.first-polaroid {
  position: relative;
  width: 82%;
  margin: 0 auto;
  padding: 10px 10px 18px;
  background: #fff;
  box-shadow:
    0 18px 42px rgba(54, 71, 78, 0.17),
    0 2px 5px rgba(60, 55, 45, 0.08);
  transform: rotate(-1.6deg);
}

.first-polaroid .memory-photo {
  width: 100%;
  aspect-ratio: 1 / 0.9;
  background-color: #dfe9ea;
}

.first-clip {
  position: absolute;
  z-index: 4;
  top: -16px;
  left: calc(50% - 6px);
  width: 12px;
  height: 27px;
  border-radius: 2px;
  background: linear-gradient(90deg, #cbb187, #f3dfb4 45%, #bda278);
  box-shadow: 0 2px 3px rgba(86, 69, 42, 0.3);
}

.first-polaroid-caption {
  padding: 13px 4px 0;
  text-align: center;
}

.first-polaroid-caption span,
.first-polaroid-caption b {
  display: block;
}

.first-polaroid-caption span {
  color: #99a5a8;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.first-polaroid-caption b {
  margin-top: 6px;
  color: #62645f;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.5;
}

.first-letter {
  position: relative;
  z-index: 3;
  width: 90%;
  max-height: 172px;
  margin: -3px auto 0;
  padding: 25px 22px 18px;
  overflow: hidden;
  color: #6b675c;
  background: #fff8e8;
  border: 1px solid rgba(214, 197, 159, 0.55);
  box-shadow: 0 12px 30px rgba(70, 65, 52, 0.11);
  transform: rotate(0.8deg);
}

.letter-tape {
  position: absolute;
  top: -5px;
  left: 50%;
  width: 60px;
  height: 18px;
  background: rgba(239, 218, 160, 0.58);
  transform: translateX(-50%) rotate(-2deg);
}

.letter-to {
  margin: 0 0 11px;
  color: #a1844c;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.5;
}

.letter-body {
  margin: 0;
  white-space: pre-line;
  word-break: keep-all;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.8;
}

.first-letter-fade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 80px;
  background: linear-gradient(transparent, #fff8e8 67%);
  pointer-events: none;
}

.first-letter-more {
  position: absolute;
  right: 14px;
  bottom: 10px;
  z-index: 2;
  padding: 7px 8px;
  color: #7da5b6;
  font-size: 12px;
  font-weight: 800;
  background: rgba(255, 248, 232, 0.94);
  border: 0;
  cursor: pointer;
}

.journey-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: min(100%, 380px);
  margin: 28px auto 0;
}

.journey-prev,
.journey-next {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 56px;
  padding: 0 18px;
  border: 0;
  border-radius: 16px;
  text-align: center;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.journey-prev {
  color: #6b8794;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 6px 18px rgba(64, 103, 124, 0.08);
}

.journey-prev:disabled {
  cursor: default;
  color: #b5c3ca;
  background: rgba(255, 255, 255, 0.58);
  opacity: 1;
}

.journey-next {
  color: white;
  background: #79bdf0;
  box-shadow: 0 10px 24px rgba(76, 159, 214, 0.22);
}

.memory-photo {
  background-repeat: no-repeat;
  background-color: #dbe8ec;
}

.album-section {
  position: relative;
  padding: 64px 18px 42px;
  background: linear-gradient(180deg, #fffdf8 0%, #fbfdfb 72%, #eff9ff 100%);
}

.section-copy {
  width: min(100%, 390px);
  margin: 0 auto 26px;
  text-align: center;
}

.section-copy h2 {
  margin: 0;
  color: #223239;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.28;
  letter-spacing: -0.03em;
}

.section-copy p:not(.tiny-title) {
  margin: 12px 0 0;
  color: #85949a;
  font-size: 12px;
}

.year-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  width: min(100%, 360px);
  margin: 0 auto 24px;
  padding: 6px;
  border-radius: 18px;
  background: #edf5f7;
  box-shadow: inset 0 0 0 1px rgba(159, 190, 199, 0.2);
}

.year-tab {
  min-height: 52px;
  color: #91a3aa;
  background: transparent;
  border: 0;
  border-radius: 14px;
  cursor: pointer;
}

.year-tab strong,
.year-tab span {
  display: block;
}

.year-tab strong {
  color: inherit;
  font-size: 13px;
  letter-spacing: 0.08em;
}

.year-tab span {
  margin-top: 3px;
  font-size: 9px;
}

.year-tab.active {
  color: #5d9bd2;
  background: white;
  box-shadow: 0 8px 18px rgba(81, 124, 144, 0.11);
}

.gallery-frame {
  width: min(100%, 390px);
  margin: 0 auto;
  padding: 7px;
  background: #faf7ef;
  border: 1px solid #e5ded0;
  box-shadow: 0 22px 54px rgba(44, 55, 58, 0.11);
}

.frame-inner {
  padding: 22px 16px 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #ded8ca;
}

.frame-label {
  margin-bottom: 18px;
  color: #9aa5a9;
  text-align: center;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.frame-label span {
  color: #648ec2;
  font-weight: 700;
}

.hanging-gallery {
  display: grid;
  gap: 18px;
}

.memory-row {
  position: relative;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: start;
  gap: 11px;
}

.rope {
  position: absolute;
  top: 4px;
  right: 0;
  left: 0;
  height: 1px;
  background: rgba(198, 187, 160, 0.62);
}

.gallery-item {
  position: relative;
  z-index: 1;
  display: block;
  padding: 0;
  text-align: center;
  background: transparent;
  border: 0;
  cursor: pointer;
  transition:
    transform 0.22s ease,
    filter 0.22s ease;
}

.gallery-item:hover,
.gallery-item:focus-visible {
  z-index: 3;
  filter: brightness(1.03);
  transform: translateY(-4px) scale(1.04) !important;
}

.polaroid {
  padding: 6px 6px 10px;
  background: white;
  box-shadow: 0 9px 18px rgba(42, 43, 36, 0.13);
}

.polaroid .memory-photo {
  display: block;
  aspect-ratio: 1 / 0.86;
}

.polaroid-month {
  display: block;
  margin-top: 7px;
  color: #716f68;
  font-size: 10px;
  font-weight: 600;
}

.clip {
  position: absolute;
  z-index: 2;
  top: -8px;
  left: calc(50% - 4px);
  width: 8px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(90deg, #c8ad7c, #ead8aa 45%, #b89a62);
  box-shadow: 0 2px 3px rgba(86, 69, 42, 0.25);
}

.note-card {
  display: flex;
  aspect-ratio: 1 / 1.1;
  flex-direction: column;
  justify-content: space-between;
  padding: 13px 10px 10px;
  box-shadow: 0 8px 16px rgba(42, 43, 36, 0.09);
  overflow: hidden;
}

.note-card b,
.note-card span,
.note-card small {
  display: block;
  text-align: left;
}

.note-card b {
  color: #7d8277;
  font-size: 8px;
  font-weight: 700;
  line-height: 1.35;
}

.note-card span {
  margin-top: 6px;
  color: #5f675f;
  word-break: keep-all;
  font-size: 10.5px;
  font-weight: 700;
  line-height: 1.42;
}

.note-card small {
  margin-top: 6px;
  color: #8c948e;
  text-align: right;
  font-size: 7px;
}

.note-1 {
  background: #e9f4e9;
}

.note-2 {
  background: #fff2ce;
}

.note-3 {
  background: #eeeafb;
}

.frame-footer {
  display: flex;
  justify-content: center;
  gap: 7px;
  margin-top: 20px;
  color: #95a1a4;
  font-size: 9px;
}

.album-caption {
  margin: 32px 0 0;
  color: #6f777a;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.8;
}

.ending {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 72dvh;
  padding: 76px 20px 68px;
  text-align: center;
  background: #eff9ff;
}

.heart-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #d8b95a;
}

.heart-line::before,
.heart-line::after {
  width: 34px;
  height: 1px;
  background: #bfd6df;
  content: '';
}

.ending p {
  margin: 18px 0 10px;
  color: #91a7b1;
  font-size: 12px;
}

.ending h2 {
  margin: 0;
  color: #223239;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.42;
  letter-spacing: -0.03em;
}

.collage-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: min(100%, 320px);
  min-height: 56px;
  margin-top: 30px;
  padding: 0 22px;
  color: white;
  text-align: center;
  background: #79bdf0;
  border: 0;
  border-radius: 17px;
  box-shadow: 0 14px 30px rgba(76, 159, 214, 0.22);
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.back-button {
  min-width: 116px;
  margin-top: 16px;
  padding: 12px 22px;
  color: #7f939d;
  background: transparent;
  border: 1px solid rgba(127, 147, 157, 0.32);
  border-radius: 14px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.list-button {
  width: min(100%, 320px);
  min-height: 56px;
  margin-top: 68px;
  color: white;
  background: #79bdf0;
  border: 0;
  border-radius: 15px;
  box-shadow: 0 10px 24px rgba(76, 159, 214, 0.2);
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.list-button + .back-button {
  margin-top: 12px;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(34, 45, 49, 0.58);
  backdrop-filter: blur(8px);
  animation: fade-in 0.25s ease;
}

.memory-modal,
.collage-modal {
  position: relative;
  width: min(390px, 100%);
  max-height: calc(100dvh - 36px);
  overflow-y: auto;
  background: #fffefb;
  border-radius: 30px;
  box-shadow: 0 30px 70px rgba(20, 31, 36, 0.26);
  animation: rise 0.35s ease;
}

.modal-close {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 3;
  width: 34px;
  height: 34px;
  color: white;
  font-size: 23px;
  line-height: 1;
  background: rgba(30, 39, 42, 0.28);
  border: 0;
  border-radius: 50%;
  backdrop-filter: blur(8px);
  cursor: pointer;
}

.modal-photo-wrap {
  position: relative;
  height: 360px;
  overflow: hidden;
  border-radius: 30px 30px 0 0;
}

.modal-photo-wrap .memory-photo {
  width: 100%;
  height: 100%;
}

.modal-photo-wrap::after {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 35%;
  background: linear-gradient(transparent, rgba(20, 30, 34, 0.48));
  content: '';
}

.modal-date {
  position: absolute;
  bottom: 18px;
  left: 22px;
  z-index: 2;
  color: white;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.12em;
}

.modal-body {
  padding: 24px 22px 26px;
}

.modal-kicker {
  margin: 0 0 7px;
  color: #80aec5;
  font-size: 8px;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.modal-body h2 {
  margin: 0;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.35;
}

.modal-short {
  margin: 8px 0 20px;
  color: #879399;
  font-size: 11px;
}

.letter-preview {
  position: relative;
  padding: 18px 16px;
  color: #64635d;
  background: #fffaf0;
  border: 1px solid #eee5d2;
}

.letter-preview b {
  display: block;
  color: #a28c58;
  font-size: 12px;
  font-weight: 800;
}

.letter-preview p {
  margin: 12px 0 0;
  white-space: pre-line;
  font-size: 11px;
  line-height: 1.9;
}

.letter-toggle {
  width: 100%;
  margin-top: 10px;
  padding: 12px;
  color: #5fa9d6;
  background: transparent;
  border: 0;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}

.collage-backdrop {
  background: rgba(42, 52, 55, 0.72);
}

.collage-modal {
  width: min(560px, 100%);
  padding: 32px 20px 24px;
  text-align: center;
  background: #f8f7f2;
}

.collage-modal .modal-close {
  color: #637178;
  background: #edf1f1;
}

.tiny-title {
  margin: 0 0 10px;
  color: #8eb7ca;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.24em;
}

.collage-modal h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.32;
}

.collage-desc {
  margin: 9px 0 22px;
  color: #90999b;
  font-size: 10px;
}

.final-collage {
  position: relative;
  height: clamp(360px, 90vw, 470px);
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.22) 1px, transparent 1px),
    linear-gradient(0deg, rgba(148, 129, 103, 0.12) 1px, transparent 1px),
    linear-gradient(135deg, #ece2d2 0%, #faf2df 42%, #d9c8ae 100%);
  background-size:
    16px 16px,
    16px 16px,
    cover;
  border: 10px solid #fffdf8;
  box-shadow: 0 18px 42px rgba(51, 51, 45, 0.16);
}

.collage-photo-button {
  position: absolute;
  display: flex;
  aspect-ratio: 0.88;
  padding: clamp(6px, 1.7vw, 9px) clamp(6px, 1.7vw, 9px) clamp(20px, 5vw, 30px);
  overflow: hidden;
  background: #fffdf8;
  border: 0;
  box-shadow: 0 12px 24px rgba(50, 38, 22, 0.24);
  cursor: pointer;
}

.final-collage .memory-photo {
  display: block;
  width: 100%;
  height: 100%;
  background-color: #1a1a1a;
}

.collage-photo-button:nth-child(1) {
  top: 8%;
  left: 28%;
  z-index: 7;
  width: 44%;
  transform: rotate(4deg);
}

.collage-photo-button:nth-child(2) {
  top: 13%;
  left: -18%;
  z-index: 2;
  width: 36%;
  transform: rotate(-17deg);
}

.collage-photo-button:nth-child(3) {
  top: 12%;
  right: -12%;
  z-index: 3;
  width: 42%;
  transform: rotate(15deg);
}

.collage-photo-button:nth-child(4) {
  top: 42%;
  left: 4%;
  z-index: 5;
  width: 40%;
  transform: rotate(12deg);
}

.collage-photo-button:nth-child(5) {
  top: 45%;
  right: 3%;
  z-index: 6;
  width: 43%;
  transform: rotate(-8deg);
}

.collage-photo-button:nth-child(6) {
  top: 61%;
  left: 28%;
  z-index: 8;
  width: 44%;
  transform: rotate(6deg);
}

.collage-photo-button:nth-child(7) {
  top: -13%;
  left: 3%;
  z-index: 1;
  width: 38%;
  transform: rotate(-7deg);
}

.collage-photo-button:nth-child(8) {
  top: -10%;
  right: 9%;
  z-index: 1;
  width: 37%;
  transform: rotate(9deg);
}

.collage-photo-button:nth-child(9) {
  bottom: -20%;
  left: -6%;
  z-index: 2;
  width: 42%;
  transform: rotate(-10deg);
}

.collage-photo-button:nth-child(10) {
  bottom: -23%;
  right: -5%;
  z-index: 2;
  width: 39%;
  transform: rotate(13deg);
}

.collage-photo-button:nth-child(11) {
  bottom: -27%;
  left: 32%;
  z-index: 1;
  width: 36%;
  transform: rotate(-3deg);
}

.collage-photo-button:nth-child(12) {
  top: 70%;
  right: 28%;
  z-index: 4;
  width: 34%;
  transform: rotate(-16deg);
}

@media (max-width: 430px) {
  .collage-modal {
    padding: 30px 14px 22px;
  }

  .collage-modal h2 {
    font-size: 24px;
  }

  .collage-desc {
    margin-bottom: 16px;
  }

  .final-collage {
    height: clamp(330px, 94vw, 400px);
    border-width: 8px;
  }

  .collage-photo-button:nth-child(1) {
    top: 10%;
    left: 27%;
    width: 46%;
  }

  .collage-photo-button:nth-child(4) {
    top: 43%;
    left: 1%;
  }

  .collage-photo-button:nth-child(5) {
    top: 46%;
    right: 0;
  }

  .collage-photo-button:nth-child(6) {
    top: 64%;
  }
}

.save-button {
  width: 100%;
  margin-top: 18px;
  padding: 14px;
  color: white;
  background: #79bdf0;
  border: 0;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
}

@media (min-width: 700px) {
  .first-memory {
    padding-top: 78px;
  }

  .first-keepsake {
    width: 420px;
  }

  .first-polaroid {
    width: 86%;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    scroll-behavior: auto !important;
    transition: none !important;
    animation: none !important;
  }
}
</style>
