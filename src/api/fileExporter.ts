import { Capacitor, registerPlugin } from '@capacitor/core'

export interface ExportableFile {
  url: string
  fileName: string
}

interface FileExporterPlugin {
  exportFiles(options: { files: ExportableFile[]; authorization?: string }): Promise<{
    saved: number
    failed: number
    cancelled: boolean
  }>
}

const FileExporter = registerPlugin<FileExporterPlugin>('FileExporter')

export function canExportFilesNatively(): boolean {
  return Capacitor.getPlatform() === 'android'
}

export async function exportFilesNatively(files: ExportableFile[]) {
  const token = localStorage.getItem('token')
  return FileExporter.exportFiles({
    files,
    authorization: token ? `Bearer ${token}` : undefined,
  })
}
