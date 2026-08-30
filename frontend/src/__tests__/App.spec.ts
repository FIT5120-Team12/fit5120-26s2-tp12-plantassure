import { describe, it, expect } from 'vitest'

import { mount } from '@vue/test-utils'
import HomeView from '@/views/HomeView.vue'

describe('HomeView', () => {
  it('renders the home placeholder', () => {
    const wrapper = mount(HomeView)
    expect(wrapper.get('h1').text()).toBe('PlantAssure Home')
  })
})
