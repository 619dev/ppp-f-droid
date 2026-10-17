// Shared messaging trust records use Android Keystore-backed storage on this client.
export {
  getSecureSecret as getBrowserSecret,
  setSecureSecret as setBrowserSecret,
  deleteSecureSecret as deleteBrowserSecret,
} from '../api/secure-storage'
