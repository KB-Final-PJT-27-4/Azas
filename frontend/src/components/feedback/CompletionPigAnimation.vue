<script setup lang="ts">
import pigUrl from '@/assets/images/accounts/complete-pig.png'
import starUrl from '@/assets/images/accounts/complete-star.png'
import diamondUrl from '@/assets/images/accounts/complete-diamond.png'
import circleUrl from '@/assets/images/accounts/complete-circle.png'
</script>

<template>
  <div class="scene" aria-hidden="true">
    <img class="pig" :src="pigUrl" alt="" />
    <img class="decoration star star-left" :src="starUrl" alt="" />
    <img class="decoration star star-right" :src="starUrl" alt="" />
    <img class="decoration diamond diamond-left" :src="diamondUrl" alt="" />
    <img class="decoration diamond diamond-right" :src="diamondUrl" alt="" />
    <img class="decoration circle circle-left" :src="circleUrl" alt="" />
    <img class="decoration circle circle-right" :src="circleUrl" alt="" />
    <span class="check">
      <svg width="38" height="38" viewBox="0 0 24 24" fill="none">
        <path pathLength="1" d="M4 12.5L9.2 17.5L20 6.5" />
      </svg>
    </span>
  </div>
</template>

<style scoped>
.scene { position: relative; width: 248px; height: 168px; }
.pig { position: absolute; z-index: 2; top: 0; left: 9px; width: 230px; height: 168px; object-fit: contain; }
.decoration { position: absolute; z-index: 1; object-fit: contain; }
.star { width: 45px; height: 45px; }
.star-left { top: 19px; left: -7px; }
.star-right { right: -6px; bottom: 19px; width: 40px; height: 40px; }
.diamond { width: 25px; height: 25px; }
.diamond-left { left: 8px; bottom: 33px; }
.diamond-right { top: 65px; right: 13px; width: 22px; height: 22px; }
.circle { width: 18px; height: 18px; }
.circle-left { top: 75px; left: 21px; }
.circle-right { top: 94px; right: -1px; width: 14px; height: 14px; }
.check { position: absolute; z-index: 3; top: 2px; right: 10px; display: grid; width: 58px; height: 58px; color: white; background: linear-gradient(155deg, #61c8f5 2%, #2d8dec 82%); border-radius: 50%; box-shadow: 0 8px 18px rgb(45 141 236 / 24%); place-items: center; }
.check path { fill: none; stroke: currentColor; stroke-width: 3.4; stroke-linecap: round; stroke-linejoin: round; }

@media (prefers-reduced-motion: no-preference) {
  .pig { animation: pig-arrive 680ms cubic-bezier(0.16, 1, 0.3, 1) both; }
  .decoration { animation: spark-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) both, spark-float 2.8s ease-in-out 1.15s infinite alternate; }
  .star-left { animation-delay: 180ms, 1.15s; }
  .star-right { animation-delay: 290ms, 1.35s; }
  .diamond-left { animation-delay: 350ms, 1.25s; }
  .diamond-right { animation-delay: 430ms, 1.5s; }
  .circle-left { animation-delay: 500ms, 1.4s; }
  .circle-right { animation-delay: 570ms, 1.6s; }
  .check { animation: check-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) 260ms both; }
  .check path { stroke-dasharray: 1; stroke-dashoffset: 1; animation: check-draw 300ms cubic-bezier(0.25, 0.8, 0.3, 1) 560ms forwards; }
}

@keyframes pig-arrive {
  0% { opacity: 0; transform: translateY(14px) scale(0.78); }
  65% { opacity: 1; transform: translateY(-3px) scale(1.035); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}
@keyframes spark-arrive {
  0% { opacity: 0; transform: translate(0, 10px) scale(0.2) rotate(-25deg); }
  70% { opacity: 1; transform: translate(0, -2px) scale(1.18) rotate(8deg); }
  100% { opacity: 1; transform: translate(0, 0) scale(1) rotate(0); }
}
@keyframes spark-float {
  from { transform: translateY(0) rotate(-3deg); }
  to { transform: translateY(-5px) rotate(5deg); }
}
@keyframes check-arrive {
  0% { opacity: 0; transform: scale(0.35) rotate(-12deg); }
  68% { opacity: 1; transform: scale(1.12) rotate(3deg); }
  100% { opacity: 1; transform: scale(1) rotate(0); }
}
@keyframes check-draw { to { stroke-dashoffset: 0; } }
</style>
