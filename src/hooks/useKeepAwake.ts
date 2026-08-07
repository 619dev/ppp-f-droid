import { useEffect } from 'react'
import { setKeepAwake } from '../api/keepAwake'

let activeKeepAwakeCount = 0

function updateKeepAwakeState() {
  void setKeepAwake(activeKeepAwakeCount > 0)
}

/** Keeps the Android screen awake while any active lifecycle requires it. */
export function useKeepAwake(active: boolean) {
  useEffect(() => {
    if (!active) return

    activeKeepAwakeCount += 1
    updateKeepAwakeState()

    return () => {
      activeKeepAwakeCount = Math.max(0, activeKeepAwakeCount - 1)
      updateKeepAwakeState()
    }
  }, [active])
}
