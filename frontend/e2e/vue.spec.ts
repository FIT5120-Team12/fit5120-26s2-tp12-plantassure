import { test, expect } from '@playwright/test'

// See here how to get started:
// https://playwright.dev/docs/intro
test('visits the PlantAssure home route', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('h1')).toHaveText('PlantAssure Home')
})

test('visits a plant assessment route', async ({ page }) => {
  await page.goto('/plants/1/assessment')
  await expect(page.locator('h1')).toHaveText('Plant Assessment')
})
