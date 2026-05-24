<template>
  <router-view v-slot="{ Component, route }">
    <keep-alive include="ReimbursementList">
      <component
        :is="Component"
        :key="route.meta.keepAlive ? route.name : route.fullPath"
      />
    </keep-alive>
  </router-view>
</template>
