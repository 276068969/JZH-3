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

export interface StationStatus {
  stationId: number
  stationName: string
  district: string
  address: string
  phone: string
  todayInspectionCount: number
  passedCount: number
  failedCount: number
  passRate: number
  lastInspectionTime: string
  runningStatus: string
}

export interface Announcement {
  id: number
  title: string
  content?: string
  type?: string
  publishStatus?: string
  publisher?: string
  publishTime?: string
  createTime?: string
  updateTime?: string
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

export interface PollutantLimitRule {
  id?: number
  fuelType: string
  emissionStandard: string
  coLimit: number
  hcLimit: number
  noxLimit: number
  opacityLimit: number
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface EnvironmentalJudgmentResult {
  environmentalStatus: string
  statusLevel: string
  exceededItems: Array<{
    pollutant: string
    value: number
    limit: number
    exceedRatio: number
  }>
  suggestion: string
  appliedStandard: string
  fuelType?: string
  emissionStandard?: string
  coLimit?: number
  hcLimit?: number
  noxLimit?: number
  opacityLimit?: number
}

export const login = (username: string, password: string) =>
  http.post('/auth/login', { username, password })

export const fetchCurrentUser = () => http.get('/auth/me')

export const fetchDashboard = (days?: number) => http.get('/dashboard', { params: days ? { days } : {} })

export const searchVehicle = (keyword: string) =>
  http.get<ApiResponse<Vehicle>>('/vehicles/search', { params: { keyword } })

export const fetchInspections = (plateNumber?: string) =>
  http.get<InspectionRecord[]>('/inspections', { params: { plateNumber } })

export const fetchInspectionDetail = (inspectionNo: string) =>
  http.get<InspectionRecord>('/inspections/detail', { params: { inspectionNo } })

export const fetchStations = (district?: string, status?: string) =>
  http.get<Station[]>('/stations', { params: { district, status } })

export const fetchStationStatuses = () =>
  http.get<StationStatus[]>('/stations/status')

export const fetchAnnouncements = () => http.get<Announcement[]>('/announcements')

export const fetchAnnouncementDetail = (id: number) =>
  http.get<Announcement>('/announcements/detail', { params: { id } })

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

export interface CreateInspectionRequest {
  plateNumber: string
  stationName: string
  coValue: number
  hcValue: number
  noxValue: number
  opacityValue: number
  inspector: string
}

export const createInspection = (data: CreateInspectionRequest) =>
  http.post('/inspections/create', data)

export const fetchPollutantLimitRules = (params?: {
  page?: number
  pageSize?: number
  fuelType?: string
  emissionStandard?: string
  status?: string
}) => http.get('/pollutant-limit-rules/list', { params })

export const fetchAllPollutantLimitRules = () =>
  http.get<PollutantLimitRule[]>('/pollutant-limit-rules/all')

export const fetchPollutantLimitRuleDetail = (id: number) =>
  http.get<PollutantLimitRule>('/pollutant-limit-rules/detail', { params: { id } })

export const queryPollutantLimitRule = (fuelType: string, emissionStandard: string) =>
  http.get<PollutantLimitRule>('/pollutant-limit-rules/query', { params: { fuelType, emissionStandard } })

export const createPollutantLimitRule = (data: PollutantLimitRule) =>
  http.post('/pollutant-limit-rules/create', data)

export const updatePollutantLimitRule = (data: PollutantLimitRule) =>
  http.post('/pollutant-limit-rules/update', data)

export const deletePollutantLimitRule = (id: number) =>
  http.post('/pollutant-limit-rules/delete', null, { params: { id } })

export const fetchFuelTypes = () => http.get<string[]>('/pollutant-limit-rules/fuel-types')

export const fetchEmissionStandards = () => http.get<string[]>('/pollutant-limit-rules/emission-standards')

export const judgeEnvironmental = (
  record: InspectionRecord,
  params?: { fuelType?: string; emissionStandard?: string }
) => http.post<EnvironmentalJudgmentResult>('/inspections/judge', record, { params })

export const judgeEnvironmentalByNo = (
  inspectionNo: string,
  params?: { fuelType?: string; emissionStandard?: string }
) => http.get<EnvironmentalJudgmentResult>('/inspections/judge', { params: { inspectionNo, ...params } })
