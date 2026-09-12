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
        <label className="text-sm font-semibold text-[#4A4A40] font-body">
          {label}
        </label>
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
              ? 'bg-[#5D7052]/8 border-[#5D7052] scale-[1.01]'
              : selectedFile
              ? 'bg-[#E6DCCD]/40 border-[#5D7052]/50'
              : 'bg-white/40 border-[#DED8CF] hover:border-[#5D7052]/50 hover:bg-[#5D7052]/5'
          }
          ${errorText ? 'border-[#ff4757]' : ''}
        `}
      >
        {selectedFile ? (
          <div className="flex items-center justify-between w-full max-w-md p-3 rounded-2xl bg-[#F0EBE5]">
            <div className="flex items-center gap-3 overflow-hidden">
              <div className="w-9 h-9 rounded-xl bg-[#5D7052]/15 flex items-center justify-center text-[#5D7052]">
                <FileCode className="w-5 h-5" />
              </div>
              <div className="flex flex-col text-left overflow-hidden">
                <span className="text-sm font-semibold font-body text-[#2C2C24] truncate">
                  {selectedFile.name}
                </span>
                <span className="text-xs font-body text-[#78786C]">
                  {(selectedFile.size / 1024).toFixed(1)} KB
                </span>
              </div>
            </div>
            <button
              type="button"
              onClick={handleClear}
              className="p-1.5 rounded-full hover:bg-[#A85448]/10 text-[#78786C] hover:text-[#A85448] transition-colors"
              title="Remove file"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-3 py-2">
            <div className="w-12 h-12 rounded-2xl bg-[#5D7052]/10 flex items-center justify-center text-[#5D7052]">
              <UploadCloud className="w-6 h-6" />
            </div>
            <div className="flex flex-col gap-0.5 text-center">
              <span className="text-sm font-semibold font-body text-[#2C2C24]">
                Click to browse or drop your strategy file
              </span>
              <span className="text-xs font-body text-[#78786C]">
                Supports .py, .json, .yaml — securely stored in S3
              </span>
            </div>
          </div>
        )}
      </div>

      {errorText ? (
        <p className="text-xs font-body text-[#A85448] font-semibold flex items-center gap-1">
          <span className="w-1.5 h-1.5 rounded-full bg-[#A85448] inline-block" />
          {errorText}
        </p>
      ) : helperText ? (
        <p className="text-xs font-body text-[#78786C]">{helperText}</p>
      ) : null}
    </div>
  )
}
