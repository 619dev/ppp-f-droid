import { useEffect } from 'react'
import { setKeepAwake } from '../api/keepAwake'
import { useCallContext } from '../contexts/CallContext'
import { useGroupCallContext } from '../contexts/GroupCallContext'

/** Coordinates screen wake state across direct and group calls. */
export default function CallKeepAwake() {
  const { callState } = useCallContext()
  const { status: groupCallStatus } = useGroupCallContext()
  const isCallActive = (callState !== 'idle' && callState !== 'error') || groupCallStatus !== 'idle'

  useEffect(() => {
    void setKeepAwake(isCallActive)

    return () => {
      if (isCallActive) void setKeepAwake(false)
    }
  }, [isCallActive])

  return null
}
