<template>
  <div class="complete-mark" aria-hidden="true">
    <span class="complete-mark__dot complete-mark__dot--yellow"></span>
    <span class="complete-mark__dot complete-mark__dot--red"></span>
    <span class="complete-mark__dot complete-mark__dot--blue"></span>
    <span class="complete-mark__dot complete-mark__dot--green"></span>
    <span class="complete-mark__circle">
      <svg class="complete-mark__check" width="54" height="54" viewBox="0 0 24 24" fill="none">
        <path pathLength="1" d="M4 12.5L9.2 17.5L20 6.5" />
      </svg>
    </span>
  </div>
</template>

<style scoped>
.complete-mark { position: relative; width: 142px; height: 142px; }
.complete-mark__circle { position: absolute; inset: 11px; display: grid; color: white; background: linear-gradient(155deg, #5bc6f5 2%, #2387e9 82%); border-radius: 50%; box-shadow: 0 13px 24px rgb(35 135 233 / 24%); place-items: center; }
.complete-mark__dot { position: absolute; z-index: 1; width: 10px; height: 10px; border-radius: 3px; transform: rotate(18deg); }
.complete-mark__dot--yellow { top: 35px; left: 5px; background: #ffb634; --burst-x: 66px; --burst-y: 36px; --dot-rotate: 18deg; }
.complete-mark__dot--red { top: 24px; right: 4px; background: #ff7d86; --burst-x: -57px; --burst-y: 47px; --dot-rotate: 40deg; }
.complete-mark__dot--blue { bottom: 13px; left: 19px; background: #62c9f4; --burst-x: 52px; --burst-y: -48px; --dot-rotate: 18deg; }
.complete-mark__dot--green { right: 14px; bottom: 14px; background: #65d09c; --burst-x: -47px; --burst-y: -47px; --dot-rotate: 42deg; }

@media (prefers-reduced-motion: no-preference) {
  .complete-mark__circle { animation: complete-mark-arrive 760ms cubic-bezier(0.16, 1, 0.3, 1) both, complete-mark-glow 720ms ease-out 620ms both; }
  .complete-mark__check path { fill: none; stroke: currentColor; stroke-width: 3.4; stroke-linecap: round; stroke-linejoin: round; stroke-dasharray: 1; stroke-dashoffset: 1; animation: complete-check-draw 300ms cubic-bezier(0.25, 0.8, 0.3, 1) 300ms forwards; }
  .complete-mark__dot { animation: complete-confetti-burst 760ms cubic-bezier(0.15, 0.85, 0.25, 1.18) 440ms both; }
  .complete-mark__dot--red { animation-delay: 490ms; }
  .complete-mark__dot--blue { animation-delay: 540ms; }
  .complete-mark__dot--green { animation-delay: 590ms; }
}

@keyframes complete-mark-arrive {
  0% { opacity: 0; transform: scale(0.48) rotate(-7deg); }
  58% { opacity: 1; transform: scale(1.09) rotate(2deg); }
  78% { transform: scale(0.96) rotate(-1deg); }
  100% { opacity: 1; transform: scale(1) rotate(0); }
}
@keyframes complete-mark-glow {
  0% { box-shadow: 0 13px 24px rgb(35 135 233 / 24%), 0 0 0 0 rgb(65 170 240 / 26%); }
  55%, 100% { box-shadow: 0 13px 24px rgb(35 135 233 / 24%), 0 0 0 16px rgb(65 170 240 / 0%); }
}
@keyframes complete-check-draw { to { stroke-dashoffset: 0; } }
@keyframes complete-confetti-burst {
  0% { opacity: 0; transform: translate(var(--burst-x), var(--burst-y)) scale(0.15) rotate(-80deg); }
  45% { opacity: 1; }
  72% { transform: translate(0, 0) scale(1.28) rotate(calc(var(--dot-rotate) + 16deg)); }
  100% { opacity: 1; transform: translate(0, 0) scale(1) rotate(var(--dot-rotate)); }
}
</style>
