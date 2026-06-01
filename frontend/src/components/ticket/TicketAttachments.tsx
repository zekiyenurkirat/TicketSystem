import { useEffect, useRef, useState } from 'react'
import type { AttachmentResponse } from '../../types/attachment.types'
import {
  downloadAttachment,
  getAttachmentsByTicket,
  uploadAttachment,
} from '../../api/attachment.api'

type TicketAttachmentsProps = {
  ticketId: number
}

const ALLOWED_EXT = ['pdf', 'doc', 'docx', 'png', 'jpg', 'jpeg', 'txt']
const MAX_FILE_SIZE = 10 * 1024 * 1024

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function validateFile(file: File): string | null {
  const ext = file.name.split('.').pop()?.toLowerCase() ?? ''
  if (!ALLOWED_EXT.includes(ext)) {
    return `Bu dosya türüne izin verilmiyor. İzin verilenler: ${ALLOWED_EXT.join(', ')}`
  }
  if (file.size > MAX_FILE_SIZE) {
    return 'Dosya boyutu 10 MB sınırını aşamaz.'
  }
  return null
}

function TicketAttachments({ ticketId }: TicketAttachmentsProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [attachments, setAttachments] = useState<AttachmentResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)

  const [downloadingId, setDownloadingId] = useState<number | null>(null)
  const [downloadError, setDownloadError] = useState<string | null>(null)

  function refreshList() {
    getAttachmentsByTicket(ticketId)
      .then(setAttachments)
      .catch(() => {})
  }

  useEffect(() => {
    setIsLoading(true)
    setLoadError(null)
    getAttachmentsByTicket(ticketId)
      .then(setAttachments)
      .catch((err) =>
        setLoadError(
          err instanceof Error ? err.message : 'Dosyalar yüklenirken bir hata oluştu.'
        )
      )
      .finally(() => setIsLoading(false))
  }, [])

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploadError(null)
    const error = validateFile(file)
    if (error) {
      setUploadError(error)
      setSelectedFile(null)
      if (fileInputRef.current) fileInputRef.current.value = ''
      return
    }
    setSelectedFile(file)
  }

  async function handleUpload() {
    if (!selectedFile) return
    setIsUploading(true)
    setUploadError(null)
    try {
      await uploadAttachment(ticketId, selectedFile)
      setSelectedFile(null)
      if (fileInputRef.current) fileInputRef.current.value = ''
      refreshList()
    } catch (err) {
      setUploadError(
        err instanceof Error ? err.message : 'Dosya yüklenirken bir hata oluştu.'
      )
    } finally {
      setIsUploading(false)
    }
  }

  async function handleDownload(a: AttachmentResponse) {
    setDownloadingId(a.id)
    setDownloadError(null)
    try {
      await downloadAttachment(a.id, a.fileName)
    } catch (err) {
      setDownloadError(
        err instanceof Error ? err.message : 'Dosya indirilemedi.'
      )
    } finally {
      setDownloadingId(null)
    }
  }

  return (
    <div>
      <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wide mb-3">
        Ekler
      </h4>

      {isLoading ? (
        <p className="text-xs text-slate-400 mb-4">Dosyalar yükleniyor...</p>
      ) : loadError ? (
        <p className="text-xs text-red-500 mb-4">{loadError}</p>
      ) : attachments.length === 0 ? (
        <p className="text-xs text-slate-400 mb-4">
          Bu talep için henüz dosya eklenmemiş.
        </p>
      ) : (
        <div className="space-y-1.5 mb-4">
          {attachments.map((a) => (
            <div
              key={a.id}
              className="flex items-center justify-between gap-2 px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg"
            >
              <div className="min-w-0">
                <p className="text-xs font-medium text-slate-700 truncate" title={a.fileName}>
                  {a.fileName}
                </p>
                <p className="text-xs text-slate-400">
                  {formatFileSize(a.fileSize)} · {a.uploaderFullName} · {formatDateTime(a.createdAt)}
                </p>
              </div>
              <button
                type="button"
                disabled={downloadingId === a.id}
                onClick={() => handleDownload(a)}
                className="flex-shrink-0 px-2.5 py-1 text-xs font-medium text-violet-700 bg-violet-50 hover:bg-violet-100 border border-violet-200 rounded-md transition-colors disabled:opacity-50"
              >
                {downloadingId === a.id ? '...' : 'İndir'}
              </button>
            </div>
          ))}
        </div>
      )}

      {downloadError && (
        <p className="mb-3 text-xs text-red-600">{downloadError}</p>
      )}

      <div>
        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf,.doc,.docx,.png,.jpg,.jpeg,.txt"
          disabled={isUploading}
          onChange={handleFileChange}
          className="hidden"
        />
        <div className="flex gap-2">
          <button
            type="button"
            disabled={isUploading}
            onClick={() => fileInputRef.current?.click()}
            className="flex-1 min-w-0 px-2 py-1.5 text-xs border border-slate-200 rounded-lg text-left truncate bg-white text-slate-500 hover:bg-slate-50 disabled:bg-slate-50 disabled:text-slate-400"
          >
            {selectedFile ? selectedFile.name : 'Dosya seçin...'}
          </button>
          <button
            type="button"
            disabled={!selectedFile || isUploading}
            onClick={handleUpload}
            className="flex-shrink-0 px-3 py-1.5 text-xs font-medium text-white bg-violet-600 hover:bg-violet-700 rounded-lg transition-colors disabled:opacity-50"
          >
            {isUploading ? '...' : 'Yükle'}
          </button>
        </div>

        {selectedFile && !uploadError && (
          <p className="mt-1 text-xs text-slate-400">{formatFileSize(selectedFile.size)}</p>
        )}
        {uploadError && (
          <p className="mt-1 text-xs text-red-600">{uploadError}</p>
        )}
      </div>
    </div>
  )
}

export default TicketAttachments
