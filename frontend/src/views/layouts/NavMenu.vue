<template>
  <div
      :class="{ 'is-collapsed': collapsed && !mobileMode, 'is-mobile': mobileMode }"
      class="nav-menu-container"
  >
    <div v-if="showHeader" class="menu-header">
      <slot name="header">
        <h3 class="menu-title">{{ title }}</h3>
        <el-icon
            :class="{ 'is-collapsed': collapsed }"
            class="collapse-icon"
            @click="handleToggleCollapse"
        >
          <component :is="collapsed ? 'Expand' : 'Fold'"/>
        </el-icon>
      </slot>
    </div>

    <el-menu
        :active-text-color="activeTextColor"
        :background-color="backgroundColor"
        :collapse="collapsed && !mobileMode"
        :collapse-transition="collapseTransition"
        :default-active="activeMenu"
        :ellipsis="ellipsis"
        :mode="mobileMode ? 'horizontal' : 'vertical'"
        :router="true"
        :text-color="textColor"
        :unique-opened="uniqueOpened"
        class="nav-menu"
        @select="handleSelect"
    >
      <template v-for="(item, index) in menuItems" :key="index">
        <!-- 无子菜单的情况 -->
        <el-menu-item v-if="!item.children || item.children.length === 0" :index="item.path">
          <el-icon v-if="item.icon">
            <component :is="item.icon"/>
          </el-icon>
          <template #title>
            <span>{{ item.title }}</span>
          </template>
        </el-menu-item>

        <!-- 有子菜单的情况 -->
        <el-sub-menu v-else :index="item.path">
          <template #title>
            <el-icon v-if="item.icon">
              <component :is="item.icon"/>
            </el-icon>
            <span>{{ item.title }}</span>
          </template>

          <el-menu-item
              v-for="(subItem, subIndex) in item.children"
              :key="subIndex"
              :index="subItem.path"
          >
            <el-icon v-if="subItem.icon">
              <component :is="subItem.icon"/>
            </el-icon>
            <template #title>
              <span>{{ subItem.title }}</span>
            </template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </div>
</template>

<script>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import {Expand, Fold} from '@element-plus/icons-vue'

export default {
  name: 'NavMenu',
  components: {
    Expand,
    Fold
  },
  props: {
    menuItems: {
      type: Array,
      required: true
    },
    title: {
      type: String,
      default: '导航菜单'
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    defaultCollapsed: {
      type: Boolean,
      default: false
    },
    backgroundColor: {
      type: String,
      default: '#304156'
    },
    textColor: {
      type: String,
      default: '#bfcbd9'
    },
    activeTextColor: {
      type: String,
      default: '#409EFF'
    },
    uniqueOpened: {
      type: Boolean,
      default: true
    },
    collapseTransition: {
      type: Boolean,
      default: true
    },
    ellipsis: {
      type: Boolean,
      default: true
    },
    mobileBreakpoint: {
      type: Number,
      default: 768
    }
  },
  emits: ['collapse-change', 'select'],
  setup(props, {emit}) {
    const route = useRoute()
    const collapsed = ref(props.defaultCollapsed)
    const mobileMode = ref(false)

    // 计算当前活动菜单
    const activeMenu = computed(() => {
      const {path} = route
      // 寻找匹配的菜单项
      return findMatchingMenuPath(path, props.menuItems) || path
    })

    // 递归查找匹配的菜单路径
    const findMatchingMenuPath = (path, menuItems) => {
      for (const item of menuItems) {
        if (item.path === path) {
          return item.path
        }

        if (item.children && item.children.length > 0) {
          const match = findMatchingMenuPath(path, item.children)
          if (match) return match
        }
      }
      return null
    }

    // 切换折叠状态
    const handleToggleCollapse = () => {
      collapsed.value = !collapsed.value
      emit('collapse-change', collapsed.value)
    }

    // 选择菜单项
    const handleSelect = (index, indexPath, item, routerResult) => {
      emit('select', {index, indexPath, item, routerResult})
    }

    // 检查窗口宽度并设置移动模式
    const checkIfMobile = () => {
      mobileMode.value = window.innerWidth < props.mobileBreakpoint
      // 在移动模式下，菜单始终展开
      if (mobileMode.value) {
        collapsed.value = false
      }
    }

    // 监听窗口大小变化
    onMounted(() => {
      checkIfMobile()
      window.addEventListener('resize', checkIfMobile)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', checkIfMobile)
    })

    return {
      collapsed,
      mobileMode,
      activeMenu,
      handleToggleCollapse,
      handleSelect
    }
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.nav-menu-container {
  width: var(--sidebar-width, $sidebar-width);
  transition: width 0.3s;
  background-color: var(--sidebar-background, $sidebar-background);
  height: 100%;
  overflow: hidden;
}

.nav-menu-container.is-collapsed {
  width: var(--sidebar-collapsed-width, $sidebar-collapsed-width);
}

.nav-menu-container.is-mobile {
  width: 100%;
  height: auto;

  .nav-menu {
    height: auto;
  }
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: $header-height;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-title {
  color: #fff;
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.collapse-icon {
  font-size: 18px;
  color: #fff;
  cursor: pointer;
  transition: transform 0.3s;
}

.collapse-icon.is-collapsed {
  transform: rotate(180deg);
}

.nav-menu {
  height: calc(100% - #{$header-height}); /* Ensure menu takes full available height */
  border-right: none; /* Remove default border */

  &:not(.el-menu--collapse) {
    width: 100%;
  }

  // Mobile specific styles
  &.el-menu--horizontal {
    border-bottom: none;
  }

  // Deep styling for menu items
  ::v-deep(.el-menu-item),
  ::v-deep(.el-sub-menu__title) {
    &:hover {
      background-color: var(--sidebar-hover-background, $sidebar-hover-background) !important;
    }
  }

  ::v-deep(.el-menu-item.is-active) {
    background-color: var(--sidebar-active-background, $sidebar-active-background) !important;
  }
}

// Hide text when collapsed but not in mobile mode
.el-menu--collapse {
  .el-sub-menu.is-active .el-sub-menu__title,
  .el-menu-item.is-active {
    background-color: var(--sidebar-active-background, $sidebar-active-background) !important;
  }

  // Ensure icons are centered when collapsed
  .el-menu-item,
  .el-sub-menu__title {
    justify-content: center;

    i,
    .el-icon {
      margin-right: 0;
    }
  }

  span {
    display: none;
  }

  .el-sub-menu__icon-arrow {
    display: none;
  }
}

</style> 