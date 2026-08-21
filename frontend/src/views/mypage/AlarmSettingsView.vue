<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { BellRing } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import {
  enablePushNotifications,
  getPushNotificationStatus,
  type PushNotificationStatus,
} from '@/services/pushNotifications'

type AlarmSetting = {
  id: string
  title: string
  description: string
  enabled: boolean
}

type AlarmGroup = {
  id: string
  title: string
  description: string
  settings: AlarmSetting[]
}

const { showToast } = useToast()
const pushStatus = ref<PushNotificationStatus>('default')
const isEnablingPush = ref(false)

const pushStatusMessage = computed(() => {
  switch (pushStatus.value) {
    case 'enabled':
      return '이 브라우저에서 푸시 알림을 받고 있어요.'
    case 'denied':
      return '브라우저 설정에서 알림 권한을 허용해주세요.'
    case 'unsupported':
      return '이 브라우저에서는 푸시 알림을 지원하지 않아요.'
    case 'not_configured':
      return 'Firebase Web Push 환경설정이 필요해요.'
    default:
      return '알림을 놓치지 않도록 브라우저 알림을 켜보세요.'
  }
})

const canEnablePush = computed(
  () => !['enabled', 'denied', 'unsupported', 'not_configured'].includes(pushStatus.value),
)

const refreshPushStatus = async () => {
  pushStatus.value = await getPushNotificationStatus()
}

const enablePush = async () => {
  if (!canEnablePush.value || isEnablingPush.value) return

  isEnablingPush.value = true
  try {
    await enablePushNotifications()
    await refreshPushStatus()
    showToast('푸시 알림이 설정되었습니다.', 'success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '푸시 알림 설정에 실패했습니다.'
    showToast(message, 'error')
    await refreshPushStatus()
  } finally {
    isEnablingPush.value = false
  }
}

onMounted(() => {
  void refreshPushStatus()
})

const alarmGroups = ref<AlarmGroup[]>([
  {
    id: 'finance',
    title: '금융·기록 알림',
    description: '저축 일정과 가족의 금융 기록을 알려드려요.',
    settings: [
      {
        id: 'scheduled-saving',
        title: '저축 예정 알림',
        description: '저축일과 자동이체 예정 일정을 미리 알려드려요.',
        enabled: true,
      },
      {
        id: 'time-capsule-release',
        title: '타임캡슐 공개 알림',
        description: '타임캡슐을 열 수 있는 날에 알려드려요.',
        enabled: true,
      },
    ],
  },
  {
    id: 'child',
    title: '아이 성장·활동 알림',
    description: '아이의 성장 과정과 서비스 활동을 챙겨드려요.',
    settings: [
      {
        id: 'allowance-request',
        title: '용돈 요청 알림',
        description: '아이가 용돈을 요청하면 바로 알려드려요.',
        enabled: true,
      },
      {
        id: 'pregnancy-week',
        title: '임신 주차별 알림',
        description: '주차별 아이의 성장 정보와 팁을 알려드려요.',
        enabled: true,
      },
      {
        id: 'child-limit-exceeded',
        title: '아이 한도 초과 알림',
        description: '아이가 설정한 사용 한도를 넘으면 알려드려요.',
        enabled: true,
      },
      {
        id: 'child-mission-success',
        title: '아이 미션 성공 알림',
        description: '아이가 미션을 완료한 순간을 알려드려요.',
        enabled: true,
      },
    ],
  },
])

const saveSettings = () => {
  showToast('알림 설정이 저장되었습니다.', 'success')
}
</script>

<template>
  <main
    class="h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-env(safe-area-inset-bottom))] overflow-hidden px-5 pb-20"
  >
    <form class="mt-4" @submit.prevent="saveSettings">
      <section class="mb-6 rounded-[20px] border border-[#cfe8f3] bg-[#eefaff] p-4">
        <div class="flex items-center gap-3">
          <span class="grid size-11 shrink-0 place-items-center rounded-full bg-white text-[var(--color-brand-primary)]">
            <BellRing :size="22" aria-hidden="true" />
          </span>
          <div class="min-w-0 flex-1">
            <h2 class="text-[15px] font-extrabold">브라우저 푸시 알림</h2>
            <p class="mt-1 text-[11px] leading-[1.5] text-[var(--color-text-secondary)]">
              {{ pushStatusMessage }}
            </p>
          </div>
          <button
            v-if="pushStatus !== 'enabled'"
            class="h-9 shrink-0 rounded-xl bg-[var(--color-brand-primary)] px-3 text-xs font-bold text-white disabled:cursor-not-allowed disabled:bg-[#cbd9df]"
            type="button"
            :disabled="!canEnablePush || isEnablingPush"
            @click="enablePush"
          >
            {{ isEnablingPush ? '설정 중' : '알림 켜기' }}
          </button>
          <span
            v-else
            class="shrink-0 rounded-full bg-white px-3 py-2 text-xs font-bold text-[var(--color-selected-text)]"
          >
            사용 중
          </span>
        </div>
      </section>

      <section
        v-for="(group, groupIndex) in alarmGroups"
        :key="group.id"
        :class="groupIndex ? 'mt-6' : ''"
        :aria-labelledby="`alarm-group-${group.id}`"
      >
        <div class="px-1">
          <h2
            :id="`alarm-group-${group.id}`"
            class="text-[18px] font-extrabold tracking-[-0.02em] text-[var(--color-text-primary)]"
          >
            {{ group.title }}
          </h2>

        </div>

        <ul class="mt-3 overflow-hidden rounded-[20px] border border-[#e0e9ee] bg-white px-4">
          <li
            v-for="(setting, settingIndex) in group.settings"
            :key="setting.id"
            class="flex min-h-[74px] items-center gap-4 py-3.5"
            :class="settingIndex ? 'border-t border-[#edf1f3]' : ''"
          >
            <label
              class="min-w-0 flex-1 cursor-pointer"
              :for="`alarm-${setting.id}`"
            >
              <span class="block min-w-0">
                <strong class="block text-[15px] font-bold tracking-[-0.01em] text-[var(--color-text-primary)]">
                  {{ setting.title }}
                </strong>
                <span class="mt-1 block text-[11px] leading-[1.55] text-[var(--color-text-secondary)]">
                  {{ setting.description }}
                </span>
              </span>
            </label>

            <label class="relative h-7 w-[52px] shrink-0 cursor-pointer">
              <input
                :id="`alarm-${setting.id}`"
                v-model="setting.enabled"
                class="peer sr-only"
                type="checkbox"
                role="switch"
                :aria-label="`${setting.title} ${setting.enabled ? '끄기' : '켜기'}`"
              />
              <span class="absolute inset-0 rounded-full bg-[#dfe8ed] transition-colors duration-200 peer-checked:bg-[var(--color-brand-primary)] peer-focus-visible:ring-2 peer-focus-visible:ring-[#bcecff] peer-focus-visible:ring-offset-2"></span>
              <span class="absolute top-1 left-1 size-5 rounded-full bg-white shadow-[0_1px_4px_rgba(42,70,84,0.22)] transition-transform duration-200 peer-checked:translate-x-6"></span>
            </label>
          </li>
        </ul>
      </section>

      <div
        class="pointer-events-none fixed bottom-[calc(var(--app-bottom-nav-height)+12px+env(safe-area-inset-bottom))] left-1/2 z-20 w-full max-w-[var(--app-max-width)] -translate-x-1/2 px-5"
      >
        <button
          class="pointer-events-auto min-h-14 w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors active:bg-[var(--color-brand-primary-pressed)]"
          type="submit"
        >
          알림 설정 저장
        </button>
      </div>
    </form>
  </main>
</template>
