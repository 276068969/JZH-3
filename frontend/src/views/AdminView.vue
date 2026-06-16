<template>
  <main class="page admin-page">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">环</span>
        <span>{{ sidebarTitle }}</span>
      </div>
      <el-menu :default-active="activeMenu" @select="handleMenuSelect">
        <template v-if="auth.isAdmin || auth.isRegulator">
          <el-menu-item index="dashboard">数据看板</el-menu-item>
          <el-menu-item index="records">检测记录</el-menu-item>
          <el-menu-item index="warnings">超标预警</el-menu-item>
        </template>
        <template v-if="auth.isAdmin">
          <el-menu-item index="vehicles">车辆信息</el-menu-item>
          <el-menu-item index="stations">检测站管理</el-menu-item>
          <el-menu-item index="rules">限值规则</el-menu-item>
          <el-menu-item index="logs">系统日志</el-menu-item>
        </template>
        <template v-if="auth.isRegulator">
          <el-menu-item index="logs">系统日志</el-menu-item>
        </template>
        <template v-if="auth.isStation">
          <el-menu-item index="entry">检测录入</el-menu-item>
          <el-menu-item index="records">检测记录</el-menu-item>
        </template>
        <template v-if="auth.isAdmin || auth.isRegulator || auth.isStation">
          <el-menu-item index="entry" v-if="auth.isAdmin || auth.isRegulator">检测录入</el-menu-item>
        </template>
      </el-menu>
    </aside>

    <section class="admin-main">
      <header class="admin-header">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p class="muted">{{ auth.user?.displayName }} · {{ auth.user?.role }}</p>
        </div>
        <div class="header-actions">
          <el-radio-group v-if="showTimeRange" v-model="timeRange" size="default" @change="onTimeRangeChange">
            <el-radio-button :label="7">近 7 日</el-radio-button>
            <el-radio-button :label="14">近 14 日</el-radio-button>
            <el-radio-button :label="30">近 30 日</el-radio-button>
          </el-radio-group>
          <el-button style="margin-left: 12px" @click="goHome" v-if="!auth.isUser">返回首页</el-button>
          <el-button style="margin-left: 12px" @click="logout">退出</el-button>
        </div>
      </header>

      <template v-if="activeMenu === 'dashboard'">
        <div class="grid grid-4">
          <div class="card" v-for="metric in metrics" :key="metric.label">
            <div class="muted">{{ metric.label }}</div>
            <div class="metric-value" :class="metric.highlight ? 'highlight' : ''">{{ metric.value }}</div>
          </div>
        </div>

        <section class="section admin-section">
          <div class="grid grid-3">
            <div class="card chart-panel">
              <div class="section-header">
                <h2>近 {{ timeRange }} 日检测趋势</h2>
              </div>
              <div ref="trendChart" class="chart"></div>
            </div>
            <div class="card chart-panel">
              <h2>排放标准占比</h2>
              <div ref="standardChart" class="chart"></div>
            </div>
            <div class="card">
              <div class="section-header">
                <h2>超标车辆预警</h2>
                <el-radio-group v-model="warningFilter" size="small">
                  <el-radio-button label="all">全部</el-radio-button>
                  <el-radio-button label="pending">待处置</el-radio-button>
                  <el-radio-button label="processing">处置中</el-radio-button>
                  <el-radio-button label="done">已处置</el-radio-button>
                </el-radio-group>
              </div>
              <el-table :data="filteredWarnings" height="280">
                <el-table-column prop="plateNumber" label="车牌" width="105" />
                <el-table-column prop="pollutant" label="污染物" width="90" />
                <el-table-column prop="level" label="等级" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getLevelType(row.level)" size="small" effect="light">{{ row.level }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getWarningStatusType(row.status)" size="small" effect="plain">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ row }">
                    <el-button type="primary" link size="small" @click="goToWarningHandle(row)">处置</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'records' || activeMenu === 'dashboard'">
        <section class="section admin-section" v-if="activeMenu === 'dashboard' || activeMenu === 'records'">
          <div class="card">
            <div class="section-header">
              <h2>{{ auth.isStation ? '本站检测记录' : '检测记录审核' }}</h2>
              <div class="filter-bar">
                <el-button type="primary" :icon="Search" @click="toggleAdvancedFilter">
                  {{ advancedFilterVisible.includes('filter') ? '收起筛选' : '高级筛选' }}
                </el-button>
                <el-button type="primary" :icon="Refresh" @click="loadInspectionData">刷新</el-button>
              </div>
            </div>

            <el-collapse v-model="advancedFilterVisible">
              <el-collapse-item title="筛选条件" name="filter">
                <el-form :inline="true" class="advanced-filter-form">
                  <el-form-item label="车牌号">
                    <el-input
                      v-model="filterParams.plateNumber"
                      placeholder="请输入车牌号"
                      clearable
                      style="width: 160px"
                      @keyup.enter="applyFilter"
                    />
                  </el-form-item>
                  <el-form-item label="检测站" v-if="!auth.isStation">
                    <el-select
                      v-model="filterParams.stationName"
                      placeholder="请选择检测站"
                      clearable
                      style="width: 220px"
                    >
                      <el-option
                        v-for="name in stationNames"
                        :key="name"
                        :label="name"
                        :value="name"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="检测时间">
                    <el-date-picker
                      v-model="filterParams.inspectionTimeStart"
                      type="date"
                      placeholder="开始日期"
                      value-format="YYYY-MM-DD"
                      style="width: 160px"
                    />
                    <span style="margin: 0 8px">至</span>
                    <el-date-picker
                      v-model="filterParams.inspectionTimeEnd"
                      type="date"
                      placeholder="结束日期"
                      value-format="YYYY-MM-DD"
                      style="width: 160px"
                    />
                  </el-form-item>
                  <el-form-item label="检测结果">
                    <el-select
                      v-model="filterParams.result"
                      placeholder="请选择结果"
                      clearable
                      style="width: 140px"
                    >
                      <el-option
                        v-for="r in resultOptions"
                        :key="r"
                        :label="r"
                        :value="r"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="报告状态">
                    <el-select
                      v-model="filterParams.reportStatus"
                      placeholder="请选择状态"
                      clearable
                      style="width: 140px"
                    >
                      <el-option
                        v-for="s in reportStatusOptions"
                        :key="s"
                        :label="s"
                        :value="s"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="applyFilter">查询</el-button>
                    <el-button @click="resetFilter">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-collapse-item>
            </el-collapse>

            <div class="statistics-bar" v-if="activeMenu === 'records'">
              <div class="stat-item stat-total">
                <div class="stat-label">总记录数</div>
                <div class="stat-value">{{ inspectionStatistics.total }}</div>
              </div>
              <div class="stat-item stat-passed">
                <div class="stat-label">合格</div>
                <div class="stat-value">{{ inspectionStatistics.passed }}</div>
              </div>
              <div class="stat-item stat-failed">
                <div class="stat-label">不合格</div>
                <div class="stat-value">{{ inspectionStatistics.failed }}</div>
              </div>
              <div class="stat-item stat-pending">
                <div class="stat-label">待审核</div>
                <div class="stat-value">{{ inspectionStatistics.pending }}</div>
              </div>
            </div>

            <el-table :data="filteredRecords" border stripe>
              <el-table-column prop="inspectionNo" label="检测编号" min-width="150" />
              <el-table-column prop="plateNumber" label="车牌号" width="120" />
              <el-table-column prop="stationName" label="检测站" min-width="160" v-if="!auth.isStation" />
              <el-table-column prop="inspectionTime" label="检测时间" min-width="160" />
              <el-table-column prop="result" label="结果" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.result === '合格' ? 'success' : 'danger'">{{ row.result }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reportStatus" label="报告状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.reportStatus)" effect="plain">
                    {{ row.reportStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="auditor" label="审核人" width="100" v-if="!auth.isStation" />
              <el-table-column label="操作" width="240" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="(auth.isAdmin || auth.isRegulator) && row.reportStatus === '待审核'"
                    type="success"
                    size="small"
                    @click="openAuditDialog(row, 'PASS')"
                  >
                    通过
                  </el-button>
                  <el-button
                    v-if="(auth.isAdmin || auth.isRegulator) && row.reportStatus === '待审核'"
                    type="danger"
                    size="small"
                    @click="openAuditDialog(row, 'REJECT')"
                  >
                    退回
                  </el-button>
                  <el-button
                    size="small"
                    @click="viewAuditHistory(row)"
                  >
                    {{ row.reportStatus === '待审核' ? '详情' : '审核记录' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'vehicles'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>车辆信息管理</h2>
              <el-input
                v-model="vehicleSearchKeyword"
                placeholder="搜索车牌号或 VIN"
                clearable
                style="width: 280px"
                @keyup.enter="searchVehicles"
              >
                <template #append>
                  <el-button :icon="Search" @click="searchVehicles" />
                </template>
              </el-input>
            </div>
            <el-table :data="vehicleList" border stripe>
              <el-table-column prop="plateNumber" label="车牌号" width="120" />
              <el-table-column prop="vin" label="VIN" min-width="200" />
              <el-table-column prop="vehicleType" label="车辆类型" width="120" />
              <el-table-column prop="fuelType" label="燃料类型" width="100" />
              <el-table-column prop="emissionStandard" label="排放标准" width="100" />
              <el-table-column prop="owner" label="车主" width="120" />
              <el-table-column prop="environmentalStatus" label="环保状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.environmentalStatus === '合格' ? 'success' : 'warning'">
                    {{ row.environmentalStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="viewVehicleDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'stations'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>检测站管理</h2>
              <el-button type="primary" :icon="Refresh" @click="loadStationData">刷新状态</el-button>
            </div>
            <el-table :data="stationStatusList" border stripe>
              <el-table-column prop="stationName" label="检测站名称" min-width="180" />
              <el-table-column prop="district" label="辖区" width="100" />
              <el-table-column prop="todayInspectionCount" label="今日检测量" width="120" />
              <el-table-column prop="passedCount" label="合格数" width="100">
                <template #default="{ row }">
                  <span style="color: #67c23a">{{ row.passedCount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="failedCount" label="不合格数" width="100">
                <template #default="{ row }">
                  <span style="color: #f56c6c">{{ row.failedCount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="passRate" label="合格率" width="100">
                <template #default="{ row }">
                  {{ row.passRate }}%
                </template>
              </el-table-column>
              <el-table-column prop="runningStatus" label="运行状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStationStatusType(row.runningStatus)">
                    {{ row.runningStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="lastInspectionTime" label="最后检测时间" min-width="160" />
            </el-table>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'warnings'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>超标预警管理</h2>
              <el-radio-group v-model="warningFilter" size="default">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待处置</el-radio-button>
                <el-radio-button label="processing">处置中</el-radio-button>
                <el-radio-button label="done">已处置</el-radio-button>
              </el-radio-group>
            </div>
            <el-table :data="filteredWarnings" border stripe>
              <el-table-column prop="plateNumber" label="车牌号" width="120" />
              <el-table-column prop="pollutant" label="污染物" width="100" />
              <el-table-column prop="level" label="等级" width="80">
                <template #default="{ row }">
                  <el-tag :type="getLevelType(row.level)" effect="light">{{ row.level }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="200" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getWarningStatusType(row.status)" effect="plain">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="预警时间" min-width="160" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="goToWarningHandle(row)">处置</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'rules'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>污染物限值规则管理</h2>
              <div class="filter-bar">
                <el-select v-model="ruleFuelFilter" placeholder="燃料类型" clearable style="width: 140px" @change="filterRules">
                  <el-option v-for="ft in fuelTypes" :key="ft" :label="ft" :value="ft" />
                </el-select>
                <el-select v-model="ruleStandardFilter" placeholder="排放标准" clearable style="width: 140px; margin-left: 8px" @change="filterRules">
                  <el-option v-for="es in emissionStandards" :key="es" :label="es" :value="es" />
                </el-select>
                <el-select v-model="ruleStatusFilter" placeholder="状态" clearable style="width: 120px; margin-left: 8px" @change="filterRules">
                  <el-option label="启用" value="启用" />
                  <el-option label="停用" value="停用" />
                </el-select>
                <el-button type="primary" style="margin-left: 12px" :icon="Plus" @click="openRuleDialog()">新增规则</el-button>
                <el-button :icon="Refresh" @click="loadRuleData">刷新</el-button>
              </div>
            </div>
            <el-table :data="filteredRuleList" border stripe>
              <el-table-column prop="fuelType" label="燃料类型" width="120" />
              <el-table-column prop="emissionStandard" label="排放标准" width="120" />
              <el-table-column prop="coLimit" label="CO限值(%)" width="110" align="right" />
              <el-table-column prop="hcLimit" label="HC限值(ppm)" width="120" align="right" />
              <el-table-column prop="noxLimit" label="NOx限值(ppm)" width="120" align="right" />
              <el-table-column prop="opacityLimit" label="烟度限值(m⁻¹)" width="130" align="right" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === '启用' ? 'success' : 'info'" effect="plain">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
              <el-table-column prop="updateTime" label="更新时间" min-width="160" />
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="openRuleDialog(row)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="deleteRule(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 16px; display: flex; justify-content: flex-end">
              <el-pagination
                v-model:current-page="rulePage"
                v-model:page-size="rulePageSize"
                :page-sizes="[10, 20, 50]"
                :total="ruleTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="loadRuleData"
                @current-change="loadRuleData"
              />
            </div>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'logs'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>系统操作日志</h2>
              <div class="filter-bar">
                <el-input v-model="logFilterOperator" placeholder="操作人" clearable style="width: 140px" @keyup.enter="loadLogData" />
                <el-select v-model="logFilterRole" placeholder="角色" clearable style="width: 140px; margin-left: 8px" @change="loadLogData">
                  <el-option label="平台管理员" value="平台管理员" />
                  <el-option label="监管人员" value="监管人员" />
                  <el-option label="检测站工作人员" value="检测站工作人员" />
                  <el-option label="普通用户" value="普通用户" />
                </el-select>
                <el-select v-model="logFilterAction" placeholder="操作类型" clearable style="width: 140px; margin-left: 8px" @change="loadLogData">
                  <el-option label="登录" value="登录" />
                  <el-option label="车辆查询" value="车辆查询" />
                  <el-option label="检测记录查看" value="检测记录查看" />
                  <el-option label="审核通过" value="审核通过" />
                  <el-option label="审核退回" value="审核退回" />
                  <el-option label="预警处置" value="预警处置" />
                </el-select>
                <el-date-picker
                  v-model="logFilterStartTime"
                  type="date"
                  placeholder="开始日期"
                  value-format="YYYY-MM-DD"
                  style="width: 160px; margin-left: 8px"
                />
                <el-date-picker
                  v-model="logFilterEndTime"
                  type="date"
                  placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 160px; margin-left: 8px"
                />
                <el-button type="primary" :icon="Search" style="margin-left: 8px" @click="loadLogData">查询</el-button>
                <el-button @click="resetLogFilter">重置</el-button>
              </div>
            </div>
            <el-table :data="logList" border stripe>
              <el-table-column prop="operateTime" label="操作时间" width="170" />
              <el-table-column prop="operator" label="操作人" width="130" />
              <el-table-column prop="role" label="角色" width="130">
                <template #default="{ row }">
                  <el-tag :type="getRoleTagType(row.role)" size="small" effect="plain">{{ row.role }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="action" label="操作类型" width="130">
                <template #default="{ row }">
                  <el-tag :type="getActionTagType(row.action)" size="small">{{ row.action }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="businessObject" label="业务对象" width="120" />
              <el-table-column prop="detail" label="操作详情" min-width="220" show-overflow-tooltip />
              <el-table-column prop="result" label="结果" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.result === '成功' ? 'success' : 'danger'" size="small" effect="light">{{ row.result }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="ip" label="IP" width="140" />
            </el-table>
            <div style="margin-top: 16px; display: flex; justify-content: flex-end">
              <el-pagination
                v-model:current-page="logPage"
                v-model:page-size="logPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="logTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="loadLogData"
                @current-change="loadLogData"
              />
            </div>
          </div>
        </section>
      </template>
    </section>

    <el-dialog
      v-model="auditDialogVisible"
      :title="auditAction === 'PASS' ? '审核通过' : '审核退回'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="检测编号">{{ currentRecord?.inspectionNo }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentRecord?.plateNumber }}</el-descriptions-item>
        <el-descriptions-item label="检测站">{{ currentRecord?.stationName }}</el-descriptions-item>
        <el-descriptions-item label="检测结果">
          <el-tag :type="currentRecord?.result === '合格' ? 'success' : 'danger'">
            {{ currentRecord?.result }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="auditOpinion"
            type="textarea"
            :rows="4"
            :placeholder="auditAction === 'PASS' ? '请输入审核通过意见（选填）' : '请输入退回原因（必填）'"
            :maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button
          :type="auditAction === 'PASS' ? 'success' : 'danger'"
          :loading="auditLoading"
          @click="submitAudit"
        >
          确认{{ auditAction === 'PASS' ? '通过' : '退回' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyDialogVisible" title="审核历史记录" width="600px">
      <el-timeline v-if="auditHistory.length > 0">
        <el-timeline-item
          v-for="item in auditHistory"
          :key="item.id"
          :timestamp="item.auditTime"
          :type="item.auditAction === '通过' ? 'success' : 'danger'"
        >
          <h4 style="margin: 0 0 8px">
            <el-tag :type="item.auditAction === '通过' ? 'success' : 'danger'" size="small">
              {{ item.auditAction }}
            </el-tag>
            <span style="margin-left: 8px">{{ item.auditor }}</span>
          </h4>
          <p v-if="item.auditOpinion" class="muted" style="margin: 0">
            {{ item.auditOpinion }}
          </p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审核记录" />
    </el-dialog>

    <el-dialog
      v-model="ruleDialogVisible"
      :title="editingRule ? '编辑限值规则' : '新增限值规则'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form :model="ruleForm" :rules="ruleFormRules" ref="ruleFormRef" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="燃料类型" prop="fuelType">
              <el-select v-model="ruleForm.fuelType" placeholder="请选择燃料类型" style="width: 100%">
                <el-option v-for="ft in fuelTypes" :key="ft" :label="ft" :value="ft" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排放标准" prop="emissionStandard">
              <el-select v-model="ruleForm.emissionStandard" placeholder="请选择排放标准" style="width: 100%">
                <el-option v-for="es in emissionStandards" :key="es" :label="es" :value="es" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="CO限值(%)" prop="coLimit">
              <el-input-number v-model="ruleForm.coLimit" :min="0" :precision="3" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="HC限值(ppm)" prop="hcLimit">
              <el-input-number v-model="ruleForm.hcLimit" :min="0" :precision="3" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="NOx限值(ppm)" prop="noxLimit">
              <el-input-number v-model="ruleForm.noxLimit" :min="0" :precision="3" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="烟度限值(m⁻¹)" prop="opacityLimit">
              <el-input-number v-model="ruleForm.opacityLimit" :min="0" :precision="3" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ruleForm.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="停用">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="ruleForm.remark" type="textarea" :rows="3" :maxlength="500" show-word-limit placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSubmitting" @click="submitRule">确认保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Plus } from '@element-plus/icons-vue'
import {
  auditInspection,
  fetchAuditRecords,
  fetchDashboard,
  fetchInspections,
  fetchInspectionsWithFilter,
  fetchStationNames,
  fetchWarnings,
  fetchStationStatuses,
  fetchStations,
  searchVehicle,
  fetchPollutantLimitRules,
  createPollutantLimitRule,
  updatePollutantLimitRule,
  deletePollutantLimitRule,
  fetchFuelTypes,
  fetchEmissionStandards,
  fetchSystemLogs,
  type AuditRecord,
  type InspectionRecord,
  type WarningRecord,
  type StationStatus,
  type Vehicle,
  type PollutantLimitRule,
  type InspectionFilterParams,
  type InspectionStatistics,
  type SystemLog
} from '@/api/platform'
import { useAuthStore, UserRole } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const dashboard = ref<any>({})
const allRecords = ref<InspectionRecord[]>([])
const records = ref<InspectionRecord[]>([])
const warnings = ref<WarningRecord[]>([])
const stationStatusList = ref<StationStatus[]>([])
const vehicleList = ref<Vehicle[]>([])
const vehicleSearchKeyword = ref('')
const trendChart = ref<HTMLElement | null>(null)
const standardChart = ref<HTMLElement | null>(null)
const warningFilter = ref('all')
const timeRange = ref<number>(7)
const statusFilter = ref('all')

const stationNames = ref<string[]>([])
const inspectionStatistics = ref<InspectionStatistics>({
  total: 0,
  passed: 0,
  failed: 0,
  pending: 0
})

const filterParams = reactive<InspectionFilterParams>({
  plateNumber: '',
  stationName: '',
  inspectionTimeStart: '',
  inspectionTimeEnd: '',
  result: '',
  reportStatus: ''
})

const advancedFilterVisible = ref<string[]>([])
const resultOptions = ['合格', '不合格']
const reportStatusOptions = ['待审核', '已审核', '已退回']
let trendChartInstance: echarts.ECharts | null = null
let standardChartInstance: echarts.ECharts | null = null

type Metric = {
  label: string
  value: string | number
  highlight?: boolean
}

const auditDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const currentRecord = ref<InspectionRecord | null>(null)
const auditAction = ref<'PASS' | 'REJECT'>('PASS')
const auditOpinion = ref('')
const auditLoading = ref(false)
const auditHistory = ref<AuditRecord[]>([])

const fuelTypes = ref<string[]>(['汽油', '柴油', '混合动力', '天然气', '纯电动'])
const emissionStandards = ref<string[]>(['国六', '国五', '国四', '国三', '国二'])
const ruleList = ref<PollutantLimitRule[]>([])
const ruleTotal = ref(0)
const rulePage = ref(1)
const rulePageSize = ref(10)
const ruleFuelFilter = ref('')
const ruleStandardFilter = ref('')
const ruleStatusFilter = ref('')

const ruleDialogVisible = ref(false)
const editingRule = ref<PollutantLimitRule | null>(null)
const ruleSubmitting = ref(false)
const ruleFormRef = ref()
const ruleForm = reactive<PollutantLimitRule>({
  fuelType: '',
  emissionStandard: '',
  coLimit: 0,
  hcLimit: 0,
  noxLimit: 0,
  opacityLimit: 0,
  status: '启用',
  remark: ''
})
const ruleFormRules = {
  fuelType: [{ required: true, message: '请选择燃料类型', trigger: 'change' }],
  emissionStandard: [{ required: true, message: '请选择排放标准', trigger: 'change' }],
  coLimit: [{ required: true, message: '请输入CO限值', trigger: 'blur' }],
  hcLimit: [{ required: true, message: '请输入HC限值', trigger: 'blur' }],
  noxLimit: [{ required: true, message: '请输入NOx限值', trigger: 'blur' }],
  opacityLimit: [{ required: true, message: '请输入烟度限值', trigger: 'blur' }]
}

const logList = ref<SystemLog[]>([])
const logTotal = ref(0)
const logPage = ref(1)
const logPageSize = ref(20)
const logFilterOperator = ref('')
const logFilterRole = ref('')
const logFilterAction = ref('')
const logFilterStartTime = ref('')
const logFilterEndTime = ref('')

const getDefaultMenu = (): string => {
  if (auth.isStation) return 'records'
  if (auth.isAdmin || auth.isRegulator) return 'dashboard'
  return 'dashboard'
}

const activeMenu = ref(getDefaultMenu())

watch(() => auth.user?.role, () => {
  activeMenu.value = getDefaultMenu()
})

const sidebarTitle = computed(() => {
  if (auth.isStation) return '检测站工作台'
  if (auth.isRegulator) return '监管平台'
  return '监管后台'
})

const pageTitle = computed(() => {
  switch (activeMenu.value) {
    case 'dashboard': return '数据看板'
    case 'records': return auth.isStation ? '检测记录' : '检测记录审核'
    case 'vehicles': return '车辆信息'
    case 'stations': return '检测站管理'
    case 'warnings': return '超标预警'
    case 'rules': return '污染物限值规则'
    case 'logs': return '系统日志'
    case 'entry': return '检测录入'
    default: return '数据看板'
  }
})

const showTimeRange = computed(() => activeMenu.value === 'dashboard')

const metrics = computed<Metric[]>(() => {
  const periodLabel = `近${timeRange.value}日`
  const baseMetrics: Metric[] = [
    { label: `${periodLabel}检测车辆数`, value: dashboard.value.totalInspections ?? 0 },
    { label: `${periodLabel}合格车辆数`, value: dashboard.value.passedVehicles ?? 0 },
    { label: `${periodLabel}不合格车辆数`, value: dashboard.value.failedVehicles ?? 0 }
  ]
  
  if (auth.isAdmin || auth.isRegulator) {
    baseMetrics.push({ label: '待审核数量', value: dashboard.value.pendingAudit ?? 0, highlight: true })
  }
  
  baseMetrics.push({ label: `${periodLabel}超标率`, value: `${dashboard.value.exceedRate ?? 0}%` })
  
  return baseMetrics
})

const filteredRecords = computed(() => {
  let result = records.value
  const stationDisplayName = auth.user?.displayName
  if (auth.isStation && stationDisplayName) {
    result = result.filter(r => r.stationName.includes(stationDisplayName))
  }
  return result
})

watch(filteredRecords, (newRecords) => {
  if (activeMenu.value === 'records') {
    calculateLocalStatistics(newRecords)
  }
}, { immediate: true, deep: true })

const filteredWarnings = computed(() => {
  if (warningFilter.value === 'all') return warnings.value
  if (warningFilter.value === 'pending') return warnings.value.filter(w => w.status === '待处置')
  if (warningFilter.value === 'processing') return warnings.value.filter(w => w.status === '处置中')
  if (warningFilter.value === 'done') return warnings.value.filter(w => w.status === '已处置' || w.status === '已复检')
  return warnings.value
})

const getStatusType = (status: string) => {
  if (status === '已审核') return 'primary'
  if (status === '待审核') return 'warning'
  if (status === '已退回') return 'danger'
  return 'info'
}

const getLevelType = (level: string) => {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  return 'info'
}

const getWarningStatusType = (status: string) => {
  if (status === '待处置') return 'danger'
  if (status === '处置中') return 'warning'
  if (status === '已处置') return 'success'
  if (status === '已复检') return 'primary'
  return 'info'
}

const getStationStatusType = (status: string) => {
  if (status === '运行中') return 'success'
  if (status === '空闲') return 'info'
  if (status === '停运') return 'danger'
  return 'info'
}

const goToWarningHandle = (row: WarningRecord) => {
  router.push({ name: 'warning-handle', params: { id: row.id } })
}

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  if (index === 'entry') {
    router.push('/inspection-entry')
    return
  }
  if (index === 'vehicles') {
    loadVehicleData()
  }
  if (index === 'stations') {
    loadStationData()
  }
  if (index === 'rules') {
    loadRuleData()
  }
  if (index === 'records') {
    loadInspectionData()
  }
  if (index === 'logs') {
    loadLogData()
  }
}

const goHome = () => {
  router.push('/')
}

const logout = async () => {
  auth.signOut()
  await router.push('/login')
}

const loadData = async () => {
  const [dashboardResp, recordResp, warningResp] = await Promise.all([
    fetchDashboard(timeRange.value),
    fetchInspections(),
    fetchWarnings()
  ])
  dashboard.value = dashboardResp.data
  allRecords.value = recordResp.data
  records.value = recordResp.data
  warnings.value = warningResp.data
  await renderCharts()
}

const loadInspectionData = async () => {
  try {
    const params: InspectionFilterParams = {}
    if (filterParams.plateNumber?.trim()) params.plateNumber = filterParams.plateNumber.trim()
    if (filterParams.stationName?.trim()) params.stationName = filterParams.stationName.trim()
    if (filterParams.inspectionTimeStart) params.inspectionTimeStart = filterParams.inspectionTimeStart
    if (filterParams.inspectionTimeEnd) params.inspectionTimeEnd = filterParams.inspectionTimeEnd
    if (filterParams.result?.trim()) params.result = filterParams.result.trim()
    if (filterParams.reportStatus?.trim()) params.reportStatus = filterParams.reportStatus.trim()

    const { data } = await fetchInspectionsWithFilter(params)
    records.value = data.records
    allRecords.value = data.records
    await nextTick()
    calculateLocalStatistics(filteredRecords.value)
  } catch (e) {
    ElMessage.error('加载检测记录失败')
  }
}

const applyFilter = async () => {
  await loadInspectionData()
}

const resetFilter = () => {
  filterParams.plateNumber = ''
  filterParams.stationName = ''
  filterParams.inspectionTimeStart = ''
  filterParams.inspectionTimeEnd = ''
  filterParams.result = ''
  filterParams.reportStatus = ''
  loadInspectionData()
}

const toggleAdvancedFilter = () => {
  if (advancedFilterVisible.value.includes('filter')) {
    advancedFilterVisible.value = []
  } else {
    advancedFilterVisible.value = ['filter']
  }
}

const loadStationNames = async () => {
  try {
    const { data } = await fetchStationNames()
    stationNames.value = data
  } catch (e) {
    // 使用默认值
    stationNames.value = [
      '朝阳机动车环保检测站',
      '海淀机动车检测中心',
      '亦庄机动车检测站',
      '西城机动车检测站',
      '东城车辆检测中心',
      '丰台环保检测站',
      '通州机动车检测站',
      '石景山检测中心'
    ]
  }
}

const calculateLocalStatistics = (recordList: InspectionRecord[]) => {
  const total = recordList.length
  const passed = recordList.filter(r => r.result === '合格').length
  const failed = recordList.filter(r => r.result === '不合格').length
  const pending = recordList.filter(r => r.reportStatus === '待审核').length
  inspectionStatistics.value = { total, passed, failed, pending }
}

const loadStationData = async () => {
  try {
    const { data } = await fetchStationStatuses()
    stationStatusList.value = data
  } catch (e) {
    ElMessage.error('加载检测站数据失败')
  }
}

const loadVehicleData = async () => {
  try {
    const stations = await fetchStations()
    vehicleList.value = [
      {
        plateNumber: '京A12345',
        vin: 'LHGCM82633A004352',
        vehicleType: '小型轿车',
        fuelType: '汽油',
        emissionStandard: '国六',
        owner: '张先生',
        registerDate: '2021-06-18',
        environmentalStatus: '合格'
      },
      {
        plateNumber: '京B67890',
        vin: 'LSVNV2187N2039456',
        vehicleType: '轻型货车',
        fuelType: '柴油',
        emissionStandard: '国五',
        owner: '北京绿运物流',
        registerDate: '2019-03-12',
        environmentalStatus: '待复检'
      },
      {
        plateNumber: '京C24680',
        vin: 'LFPH4ACC9N1A20458',
        vehicleType: '小型客车',
        fuelType: '混合动力',
        emissionStandard: '国六',
        owner: '李女士',
        registerDate: '2022-10-09',
        environmentalStatus: '合格'
      }
    ]
  } catch (e) {
    ElMessage.error('加载车辆数据失败')
  }
}

const searchVehicles = async () => {
  if (!vehicleSearchKeyword.value.trim()) {
    loadVehicleData()
    return
  }
  try {
    const { data } = await searchVehicle(vehicleSearchKeyword.value)
    if (data.success && data.data) {
      vehicleList.value = [data.data]
    } else {
      vehicleList.value = []
      ElMessage.warning('未找到匹配的车辆')
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

const viewVehicleDetail = (row: Vehicle) => {
  router.push({ name: 'home', query: { plate: row.plateNumber } })
}

const onTimeRangeChange = async () => {
  const dashboardResp = await fetchDashboard(timeRange.value)
  dashboard.value = dashboardResp.data
  await renderCharts()
}

const filterRecords = () => {
  if (statusFilter.value === 'all') {
    records.value = [...allRecords.value]
  } else if (statusFilter.value === 'pending') {
    records.value = allRecords.value.filter(r => r.reportStatus === '待审核')
  } else if (statusFilter.value === 'audited') {
    records.value = allRecords.value.filter(r => r.reportStatus === '已审核')
  } else if (statusFilter.value === 'rejected') {
    records.value = allRecords.value.filter(r => r.reportStatus === '已退回')
  }
}

const renderCharts = async () => {
  await nextTick()
  if (trendChart.value) {
    if (trendChartInstance) {
      trendChartInstance.dispose()
    }
    trendChartInstance = echarts.init(trendChart.value)
    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: {
        type: 'category',
        data: dashboard.value.trend?.map((item: any) => item.date) || [],
        axisLabel: {
          rotate: timeRange.value > 14 ? 45 : 0,
          fontSize: 11
        }
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'line',
        smooth: true,
        data: dashboard.value.trend?.map((item: any) => item.count) || [],
        areaStyle: { opacity: 0.15 },
        lineStyle: { width: 2 },
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
  if (standardChart.value) {
    if (standardChartInstance) {
      standardChartInstance.dispose()
    }
    standardChartInstance = echarts.init(standardChart.value)
    standardChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['45%', '72%'],
        data: dashboard.value.emissionStandards || [],
        label: { formatter: '{b}: {d}%' }
      }]
    })
  }
}

const openAuditDialog = (row: InspectionRecord, action: 'PASS' | 'REJECT') => {
  currentRecord.value = row
  auditAction.value = action
  auditOpinion.value = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (auditAction.value === 'REJECT' && !auditOpinion.value.trim()) {
    ElMessage.warning('请输入退回原因')
    return
  }

  if (!currentRecord.value) return

  auditLoading.value = true
  try {
    const { data } = await auditInspection({
      inspectionNo: currentRecord.value.inspectionNo,
      action: auditAction.value,
      opinion: auditOpinion.value.trim()
    })

    if (data.success) {
      ElMessage.success(data.message)
      auditDialogVisible.value = false
      if (activeMenu.value === 'records') {
        await loadInspectionData()
      } else {
        await loadData()
      }
    } else {
      ElMessage.error(data.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败，请重试')
  } finally {
    auditLoading.value = false
  }
}

const viewAuditHistory = async (row: InspectionRecord) => {
  auditHistory.value = []
  historyDialogVisible.value = true
  try {
    const { data } = await fetchAuditRecords(row.inspectionNo)
    auditHistory.value = data
  } catch (e) {
    ElMessage.error('获取审核记录失败')
  }
}

const filteredRuleList = computed(() => ruleList.value)

const loadRuleData = async () => {
  try {
    const { data } = await fetchPollutantLimitRules({
      page: rulePage.value,
      pageSize: rulePageSize.value,
      fuelType: ruleFuelFilter.value || undefined,
      emissionStandard: ruleStandardFilter.value || undefined,
      status: ruleStatusFilter.value || undefined
    })
    ruleList.value = data.records || []
    ruleTotal.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载限值规则数据失败')
  }
}

const filterRules = async () => {
  rulePage.value = 1
  await loadRuleData()
}

const openRuleDialog = (row?: PollutantLimitRule) => {
  editingRule.value = row || null
  if (row) {
    Object.assign(ruleForm, {
      id: row.id,
      fuelType: row.fuelType,
      emissionStandard: row.emissionStandard,
      coLimit: row.coLimit,
      hcLimit: row.hcLimit,
      noxLimit: row.noxLimit,
      opacityLimit: row.opacityLimit,
      status: row.status || '启用',
      remark: row.remark || ''
    })
  } else {
    Object.assign(ruleForm, {
      fuelType: '',
      emissionStandard: '',
      coLimit: 0,
      hcLimit: 0,
      noxLimit: 0,
      opacityLimit: 0,
      status: '启用',
      remark: ''
    })
  }
  ruleDialogVisible.value = true
}

const submitRule = async () => {
  if (!ruleFormRef.value) return
  try {
    await ruleFormRef.value.validate()
  } catch {
    return
  }
  ruleSubmitting.value = true
  try {
    const { data } = editingRule.value
      ? await updatePollutantLimitRule(ruleForm)
      : await createPollutantLimitRule(ruleForm)
    if (data.success) {
      ElMessage.success(data.message)
      ruleDialogVisible.value = false
      await loadRuleData()
    } else {
      ElMessage.error(data.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败，请重试')
  } finally {
    ruleSubmitting.value = false
  }
}

const deleteRule = (row: PollutantLimitRule) => {
  ElMessageBox.confirm(
    `确定要删除「${row.fuelType} / ${row.emissionStandard}」规则吗？`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    }
  )
    .then(async () => {
      try {
        if (!row.id) return
        const { data } = await deletePollutantLimitRule(row.id)
        if (data.success) {
          ElMessage.success(data.message)
          await loadRuleData()
        } else {
          ElMessage.error(data.message)
        }
      } catch (e: any) {
        ElMessage.error(e?.message || '删除失败，请重试')
      }
    })
    .catch(() => {})
}

const loadFuelAndStandardOptions = async () => {
  try {
    const [fuelResp, standardResp] = await Promise.all([
      fetchFuelTypes(),
      fetchEmissionStandards()
    ])
    if (fuelResp.data && fuelResp.data.length > 0) {
      fuelTypes.value = fuelResp.data
    }
    if (standardResp.data && standardResp.data.length > 0) {
      emissionStandards.value = standardResp.data
    }
  } catch (e) {
    // 使用默认值
  }
}

const loadLogData = async () => {
  try {
    const { data } = await fetchSystemLogs({
      page: logPage.value,
      pageSize: logPageSize.value,
      operator: logFilterOperator.value || undefined,
      role: logFilterRole.value || undefined,
      action: logFilterAction.value || undefined,
      startTime: logFilterStartTime.value || undefined,
      endTime: logFilterEndTime.value || undefined
    })
    logList.value = data.records || []
    logTotal.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载系统日志失败')
  }
}

const resetLogFilter = () => {
  logFilterOperator.value = ''
  logFilterRole.value = ''
  logFilterAction.value = ''
  logFilterStartTime.value = ''
  logFilterEndTime.value = ''
  logPage.value = 1
  loadLogData()
}

const getRoleTagType = (role: string) => {
  if (role === '平台管理员') return 'danger'
  if (role === '监管人员') return 'warning'
  if (role === '检测站工作人员') return 'primary'
  if (role === '普通用户') return 'info'
  return 'info'
}

const getActionTagType = (action: string) => {
  if (action === '登录') return ''
  if (action === '车辆查询') return 'primary'
  if (action === '检测记录查看') return 'primary'
  if (action === '审核通过') return 'success'
  if (action === '审核退回') return 'danger'
  if (action === '预警处置') return 'warning'
  return 'info'
}

onMounted(async () => {
  await Promise.all([
    loadData(),
    loadFuelAndStandardOptions(),
    loadStationNames()
  ])
  if (activeMenu.value === 'stations') {
    await loadStationData()
  }
  if (activeMenu.value === 'vehicles') {
    await loadVehicleData()
  }
  if (activeMenu.value === 'rules') {
    await loadRuleData()
  }
  if (activeMenu.value === 'records') {
    await loadInspectionData()
  }
  if (activeMenu.value === 'logs') {
    await loadLogData()
  }
})
</script>

<style scoped>
.admin-page {
  display: grid;
  grid-template-columns: 240px 1fr;
}

.sidebar {
  min-height: 100vh;
  background: #ffffff;
  border-right: 1px solid #dfe7f2;
  padding: 18px 12px;
}

.sidebar .brand {
  margin: 0 8px 18px;
}

.admin-main {
  padding: 24px;
  overflow: hidden;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.admin-header h1,
.admin-header p,
.chart-panel h2 {
  margin: 0;
}

.admin-section {
  padding-bottom: 0;
}

.chart {
  width: 100%;
  height: 280px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.metric-value {
  font-size: 28px;
  font-weight: 600;
  margin-top: 8px;
}

.metric-value.highlight {
  color: #e6a23c;
}

.mb16 {
  margin-bottom: 16px;
}

.grid-4 {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

@media (max-width: 1200px) {
  .grid-4 {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 960px) {
  .admin-page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    min-height: auto;
  }

  .grid-4 {
    grid-template-columns: repeat(2, 1fr);
  }
}

.advanced-filter-form {
  padding: 16px 0 8px;
}

.advanced-filter-form .el-form-item {
  margin-bottom: 16px;
}

.statistics-bar {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.stat-item {
  flex: 1;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  background: #f5f7fa;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
}

.stat-total .stat-value {
  color: #409eff;
}

.stat-passed .stat-value {
  color: #67c23a;
}

.stat-failed .stat-value {
  color: #f56c6c;
}

.stat-pending .stat-value {
  color: #e6a23c;
}

.el-collapse {
  border: none;
  margin-bottom: 0;
}

.el-collapse-item :deep(.el-collapse-item__header) {
  border-bottom: none;
  padding: 0;
  height: auto;
  line-height: normal;
  margin-bottom: 8px;
}

.el-collapse-item :deep(.el-collapse-item__wrap) {
  border-bottom: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .statistics-bar {
    flex-wrap: wrap;
  }
  
  .stat-item {
    flex: 1 1 40%;
    min-width: 120px;
  }
}
</style>
