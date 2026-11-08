<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElButton, ElInput, ElMessage, ElPagination, ElTable, ElTableColumn } from 'element-plus';
import PageHeader from '../components/PageHeader.vue';
import EmployeeFormDialog from '../components/EmployeeFormDialog.vue';
import { listEmployees, type Employee } from '../api/employees';

type DialogMode = 'create' | 'edit';

const router = useRouter();

const pageSizes = [10, 20, 50];

const keyword = ref('');
const appliedKeyword = ref('');
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const items = ref<Employee[]>([]);
const loading = ref(false);
const loadError = ref<string | null>(null);

const dialogVisible = ref(false);
const dialogMode = ref<DialogMode>('create');
const editingRow = ref<Employee | null>(null);

function display(value: string | null | undefined, fallback = '-'): string {
  return value && value.trim() !== '' ? value : fallback;
}

async function load(): Promise<void> {
  loading.value = true;
  loadError.value = null;
  try {
    const result = await listEmployees({
      page: page.value,
      size: pageSize.value,
      keyword: appliedKeyword.value === '' ? undefined : appliedKeyword.value,
    });
    items.value = result.list ?? [];
    total.value = result.total ?? 0;
  } catch (error) {
    const message = error instanceof Error && error.message ? error.message : '加载员工列表失败';
    loadError.value = message;
    items.value = [];
    total.value = 0;
    ElMessage.error(message);
  } finally {
    loading.value = false;
  }
}

function search(): void {
  appliedKeyword.value = keyword.value.trim();
  page.value = 1;
  void load();
}

function reset(): void {
  keyword.value = '';
  appliedKeyword.value = '';
  page.value = 1;
  void load();
}

function handleCurrentChange(current: number): void {
  page.value = current;
  void load();
}

function handleSizeChange(size: number): void {
  pageSize.value = size;
  page.value = 1;
  void load();
}

function openCreate(): void {
  dialogMode.value = 'create';
  editingRow.value = null;
  dialogVisible.value = true;
}

function openEdit(row: Employee): void {
  dialogMode.value = 'edit';
  editingRow.value = row;
  dialogVisible.value = true;
}

function handleSaved(): void {
  void load();
}

onMounted(() => {
  void load();
});
</script>

<template>
  <main class="page">
    <PageHeader title="员工信息管理" subtitle="维护员工基础信息，支持按工号或姓名检索。">
      <template #actions>
        <ElButton @click="router.push({ name: 'home' })">返回首页</ElButton>
      </template>
    </PageHeader>

    <section class="card">
      <div class="toolbar">
        <ElInput
          v-model="keyword"
          class="toolbar-search"
          placeholder="工号／姓名"
          clearable
          @keyup.enter="search"
        />
        <ElButton type="primary" :loading="loading" @click="search">查询</ElButton>
        <ElButton :disabled="loading" @click="reset">重置</ElButton>
        <span class="toolbar-spacer" />
        <ElButton type="primary" :disabled="loading" @click="openCreate">新增员工</ElButton>
      </div>

      <div v-if="loadError" class="load-error">
        <p class="error">{{ loadError }}</p>
        <ElButton @click="load">重试</ElButton>
      </div>
      <template v-else>
        <ElTable
          v-loading="loading"
          :data="items"
          border
          stripe
          empty-text="暂无员工数据"
          class="employee-table"
        >
          <ElTableColumn prop="code" label="工号" min-width="110" />
          <ElTableColumn prop="name" label="姓名" min-width="100" />
          <ElTableColumn label="部门" min-width="110">
            <template #default="scope">{{ display(scope.row.department) }}</template>
          </ElTableColumn>
          <ElTableColumn label="职位" min-width="110">
            <template #default="scope">{{ display(scope.row.position) }}</template>
          </ElTableColumn>
          <ElTableColumn label="手机号" min-width="130">
            <template #default="scope">{{ display(scope.row.phone) }}</template>
          </ElTableColumn>
          <ElTableColumn label="入职日期" min-width="120">
            <template #default="scope">{{ display(scope.row.hireDate) }}</template>
          </ElTableColumn>
          <ElTableColumn label="在职状态" min-width="100">
            <template #default="scope">{{ display(scope.row.status, '在职') }}</template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="90" fixed="right">
            <template #default="scope">
              <ElButton link type="primary" @click="openEdit(scope.row as Employee)">编辑</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pager">
          <ElPagination
            background
            layout="total, sizes, prev, pager, next"
            :total="total"
            :current-page="page"
            :page-size="pageSize"
            :page-sizes="pageSizes"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </template>
    </section>

    <EmployeeFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :employee="editingRow"
      @saved="handleSaved"
    />
  </main>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.toolbar-search {
  width: 220px;
}

.toolbar-spacer {
  flex: 1 1 auto;
}

.employee-table {
  width: 100%;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.load-error {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  padding: 32px 8px;
}

.load-error p {
  margin: 0;
}

@media (max-width: 640px) {
  .toolbar-search {
    width: 100%;
  }
}
</style>
