import { http } from './http'

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
  plateNumber: string
  pollutant: string
  level: string
  description: string
}

export const login = (username: string, password: string) =>
  http.post('/auth/login', { username, password })

export const fetchDashboard = () => http.get('/dashboard')

export const searchVehicle = (keyword: string) =>
  http.get<Vehicle>('/vehicles/search', { params: { keyword } })

export const fetchInspections = (plateNumber?: string) =>
  http.get<InspectionRecord[]>('/inspections', { params: { plateNumber } })

export const fetchStations = () => http.get<Station[]>('/stations')

export const fetchAnnouncements = () => http.get<Announcement[]>('/announcements')

export const fetchWarnings = () => http.get<WarningRecord[]>('/warnings')

export const auditInspection = (data: AuditRequest) =>
  http.post('/inspections/audit', data)

export const fetchAuditRecords = (inspectionNo: string) =>
  http.get<AuditRecord[]>('/inspections/audit-records', { params: { inspectionNo } })
