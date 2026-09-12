import React, { useRef, useState } from 'react'
import { UploadCloud, FileCode, X } from 'lucide-react'

export interface FileDropzoneProps {
  label?: string
  accept?: string
  helperText?: string
  errorText?: string
  onFileSelect: (file: File | null) => void
  selectedFile: File | null
  className?: string
}

export const FileDropzone: React.FC<FileDropzoneProps> = ({
  label = 'STRATEGY FILE UPLOAD',
  accept = '.py,.json,.yaml,.yml,.txt',
  helperText = 'Supported formats: Python strategy (.py), Config JSON (.json), or YAML. Max 25MB.',
  errorText,
  onFileSelect,
  selectedFile,
  className = '',
}) => {
  const [isDragOver, setIsDragOver] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(true)
  }

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      onFileSelect(e.dataTransfer.files[0])
    }
  }

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      onFileSelect(e.target.files[0])
    }
  }

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation()
    onFileSelect(null)
    if (inputRef.current) {
      inputRef.current.value = ''
    }
  }

  return (
    <div className={`w-full flex flex-col gap-2 ${className}`}>
      <div className="flex items-center justify-between">
        <label className="text-xs font-bold uppercase tracking-wider text-[#4a5568] font-technical flex items-center justify-between">
          <span>{label}</span>
        </label>
        <span className="text-[10px] text-[#718096] font-technical">[STORAGE_S3_PAYLOAD]</span>
      </div>

      <input
        ref={inputRef}
        type="file"
        accept={accept}
        onChange={handleFileInputChange}
        className="hidden"
      />

      <div
        onClick={() => inputRef.current?.click()}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={`
          relative w-full rounded-xl p-6 flex flex-col items-center justify-center text-center cursor-pointer
          border-2 border-dashed transition-all duration-200
          ${
            isDragOver
              ? 'bg-[#d0d8e4] border-[#ff4757] scale-[1.01]'
              : selectedFile
              ? 'bg-[#d8e0ea] border-[#2ed573]/60 shadow-chassis-recessed'
              : 'bg-[#d9e0ea] border-[#babecc] shadow-chassis-recessed hover:border-[#718096]'
          }
          ${errorText ? 'border-[#ff4757]' : ''}
        `}
      >
        {selectedFile ? (
          <div className="flex items-center justify-between w-full max-w-md p-3 rounded-lg bg-[#e0e5ec] shadow-chassis-sharp">
            <div className="flex items-center gap-3 overflow-hidden">
              <div className="w-9 h-9 rounded-lg bg-[#2ed573]/20 flex items-center justify-center text-[#2ed573]">
                <FileCode className="w-5 h-5" />
              </div>
              <div className="flex flex-col text-left overflow-hidden">
                <span className="text-xs font-bold font-technical text-[#2d3436] truncate">
                  {selectedFile.name}
                </span>
                <span className="text-[10px] font-technical text-[#718096]">
                  {(selectedFile.size / 1024).toFixed(1)} KB • READY FOR S3
                </span>
              </div>
            </div>
            <button
              type="button"
              onClick={handleClear}
              className="p-1.5 rounded-full hover:bg-black/10 text-[#718096] hover:text-[#ff4757] transition-colors"
              title="Remove file"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-2">
            <div className="w-12 h-12 rounded-xl bg-[#e0e5ec] shadow-chassis-card flex items-center justify-center text-[#4a5568]">
              <UploadCloud className="w-6 h-6" />
            </div>
            <div className="flex flex-col gap-0.5">
              <span className="text-xs font-bold font-technical text-[#2d3436] uppercase tracking-wider">
                Click to browse or drop strategy file
              </span>
              <span className="text-[10px] font-technical text-[#718096]">
                Code will be securely encrypted & archived in S3
              </span>
            </div>
          </div>
        )}
      </div>

      {errorText ? (
        <p className="text-xs font-technical text-[#ff4757] font-semibold flex items-center gap-1">
          <span className="w-1.5 h-1.5 rounded-full bg-[#ff4757] inline-block" />
          {errorText}
        </p>
      ) : helperText ? (
        <p className="text-xs font-technical text-[#718096]">{helperText}</p>
      ) : null}
    </div>
  )
}
