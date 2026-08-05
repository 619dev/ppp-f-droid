import { Capacitor, registerPlugin } from '@capacitor/core'

interface KeepAwakePlugin {
  setKeepAwake(options: { enabled: boolean }): Promise<{ enabled: boolean }>
}

const KeepAwake = registerPlugin<KeepAwakePlugin>('KeepAwakePlugin')

/** Toggle Android's window-level keep-screen-on flag. */
export async function setKeepAwake(enabled: boolean): Promise<void> {
  if (!Capacitor.isNativePlatform() || Capacitor.getPlatform() !== 'android') return

  try {
    await KeepAwake.setKeepAwake({ enabled })
  } catch (error) {
    console.warn('[KeepAwake] Failed to update screen wake state:', error)
  }
}
