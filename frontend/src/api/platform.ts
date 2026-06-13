import { http } from './http'

export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface Vehicle {
  plateNumber: string
  vin: string
  vehicleType: string
  fuelType: string
  emissionStandard: string
  owner: string
  registerDate: string
  environmentalStatus: string
}

export interface InspectionRecord {
  inspectionNo: string
  plateNumber: string
  stationName: string
  inspectionTime: string
  coValue: number
  hcValue: number
  noxValue: number
  opacityValue: number
  result: string
  inspector: string
  reportStatus: string
  auditor?: string
  auditTime?: string
  auditOpinion?: string
}

export interface AuditRecord {
  id: number
  inspectionNo: string
  auditAction: string
  auditOpinion: string
  auditor: string
  auditTime: string
}

export interface AuditRequest {
  inspectionNo: string
  action: 'PASS' | 'REJECT'
  opinion: string
}

export interface Station {
  id: number
  name: string
  district: string
  address: string
  phone: string
  status: string
}

export interface Announcement {
  id: number
  title: string
  publishDate: string
}

export interface WarningRecord {
  id: number
  plateNumber: string
  pollutant: string
  level: string
  description: string
  status: string
  handler?: string
  handleTime?: string
  handleOpinion?: string
  reinspectRequired: boolean
  reinspectDeadline?: string
  createdAt: string
}

export interface WarningHandleRequest {
  warningId: number
  handleOpinion: string
  reinspectRequired: boolean
  reinspectDeadline?: string
  status: string
}

export const login = (username: string, password: string) =>
  http.post('/auth/login', { username, password })

export const fetchDashboard = () => http.get('/dashboard')

export const searchVehicle = (keyword: string) =>
  http.get<ApiResponse<Vehicle>>('/vehicles/search', { params: { keyword } })

export const fetchInspections = (plateNumber?: string) =>
  http.get<InspectionRecord[]>('/inspections', { params: { plateNumber } })

export const fetchInspectionDetail = (inspectionNo: string) =>
  http.get<InspectionRecord>('/inspections/detail', { params: { inspectionNo } })

export const fetchStations = () => http.get<Station[]>('/stations')

export const fetchAnnouncements = () => http.get<Announcement[]>('/announcements')

export const fetchWarnings = () => http.get<WarningRecord[]>('/warnings')

export const fetchWarningDetail = (id: number) =>
  http.get<WarningRecord>('/warnings/detail', { params: { id } })

export const handleWarning = (data: WarningHandleRequest) =>
  http.post('/warnings/handle', data)

export const fetchWarningInspections = (plateNumber: string) =>
  http.get<InspectionRecord[]>('/warnings/inspections', { params: { plateNumber } })

export const auditInspection = (data: AuditRequest) =>
  http.post('/inspections/audit', data)

export const fetchAuditRecords = (inspectionNo: string) =>
  http.get<AuditRecord[]>('/inspections/audit-records', { params: { inspectionNo } })
