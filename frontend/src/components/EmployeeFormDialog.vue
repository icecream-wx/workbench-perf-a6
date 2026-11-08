<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue';
import {
  ElButton,
  ElDatePicker,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElOption,
  ElSelect,
} from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import {
  createEmployee,
  updateEmployee,
  type Employee,
  type EmployeePayload,
  type EmployeeStatus,
} from '../api/employees';

type DialogMode = 'create' | 'edit';

interface EmployeeForm {
  code: string;
  name: string;
  department: string;
  position: string;
  phone: string;
  hireDate: string;
  status: EmployeeStatus;
}

const props = defineProps<{
  modelValue: boolean;
  mode: DialogMode;
  employee: Employee | null;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  saved: [mode: DialogMode];
}>();

const statusOptions: Array<{ value: EmployeeStatus; label: string }> = [
  { value: '在职', label: '在职' },
  { value: '离职', label: '离职' },
];

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
});

const dialogTitle = computed(() => (props.mode === 'create' ? '新增员工' : '编辑员工'));

const formRef = ref<FormInstance>();
const form = reactive<EmployeeForm>(emptyForm());
const submitting = ref(false);
const codeServerError = ref('');

const rules: FormRules<EmployeeForm> = {
  code: [
    { required: true, whitespace: true, message: '工号不能为空', trigger: 'blur' },
    { max: 32, message: '工号长度不能超过32个字符', trigger: 'blur' },
  ],
  name: [
    { required: true, whitespace: true, message: '姓名不能为空', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' },
  ],
  department: [{ max: 50, message: '部门长度不能超过50个字符', trigger: 'blur' }],
  position: [{ max: 50, message: '职位长度不能超过50个字符', trigger: 'blur' }],
  phone: [{ pattern: /^\d{11}$/, message: '手机号格式不正确', trigger: 'blur' }],
  hireDate: [{ pattern: /^\d{4}-\d{2}-\d{2}$/, message: '入职日期格式不正确', trigger: 'change' }],
  status: [{ required: true, message: '请选择在职状态', trigger: 'change' }],
};

function emptyForm(): EmployeeForm {
  return {
    code: '',
    name: '',
    department: '',
    position: '',
    phone: '',
    hireDate: '',
    status: '在职',
  };
}

function fillForm(): void {
  if (props.mode === 'edit' && props.employee) {
    Object.assign(form, {
      code: props.employee.code ?? '',
      name: props.employee.name ?? '',
      department: props.employee.department ?? '',
      position: props.employee.position ?? '',
      phone: props.employee.phone ?? '',
      hireDate: props.employee.hireDate ?? '',
      status: props.employee.status ?? '在职',
    });
  } else {
    Object.assign(form, emptyForm());
  }
  codeServerError.value = '';
  void nextTick(() => formRef.value?.clearValidate());
}

function blankToNull(value: string): string | null {
  const trimmed = value.trim();
  return trimmed === '' ? null : trimmed;
}

function toPayload(): EmployeePayload {
  return {
    code: form.code.trim(),
    name: form.name.trim(),
    department: blankToNull(form.department),
    position: blankToNull(form.position),
    phone: blankToNull(form.phone),
    hireDate: blankToNull(form.hireDate),
    status: form.status,
  };
}

function handleClosed(): void {
  formRef.value?.clearValidate();
  codeServerError.value = '';
  submitting.value = false;
}

function cancel(): void {
  if (submitting.value) {
    return;
  }
  visible.value = false;
}

async function submit(): Promise<void> {
  if (submitting.value) {
    return;
  }
  const instance = formRef.value;
  if (!instance) {
    return;
  }
  const valid = await instance.validate().catch(() => false);
  if (!valid) {
    return;
  }
  submitting.value = true;
  codeServerError.value = '';
  try {
    const payload = toPayload();
    if (props.mode === 'create') {
      await createEmployee(payload);
      ElMessage.success('新增员工成功');
      emit('update:modelValue', false);
      emit('saved', 'create');
      return;
    }
    if (!props.employee) {
      ElMessage.error('缺少待编辑的员工信息');
      return;
    }
    await updateEmployee(props.employee.id, payload);
    ElMessage.success('更新员工成功');
    emit('update:modelValue', false);
    emit('saved', 'edit');
  } catch (error) {
    const message = error instanceof Error && error.message ? error.message : '保存失败，请稍后重试';
    if (message.includes('工号已存在')) {
      codeServerError.value = message;
    }
    ElMessage.error(message);
  } finally {
    submitting.value = false;
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      fillForm();
    }
  },
);
</script>

<template>
  <ElDialog
    v-model="visible"
    :title="dialogTitle"
    width="520px"
    :close-on-click-modal="false"
    :close-on-press-escape="!submitting"
    :show-close="!submitting"
    @closed="handleClosed"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="88px" @submit.prevent>
      <ElFormItem label="工号" prop="code" :error="codeServerError || undefined">
        <ElInput v-model="form.code" placeholder="请输入工号" @input="codeServerError = ''" />
      </ElFormItem>
      <ElFormItem label="姓名" prop="name">
        <ElInput v-model="form.name" placeholder="请输入姓名" />
      </ElFormItem>
      <ElFormItem label="部门" prop="department">
        <ElInput v-model="form.department" placeholder="请输入部门" />
      </ElFormItem>
      <ElFormItem label="职位" prop="position">
        <ElInput v-model="form.position" placeholder="请输入职位" />
      </ElFormItem>
      <ElFormItem label="手机号" prop="phone">
        <ElInput v-model="form.phone" placeholder="请输入11位手机号" />
      </ElFormItem>
      <ElFormItem label="入职日期" prop="hireDate">
        <ElDatePicker
          v-model="form.hireDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择入职日期"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="在职状态" prop="status">
        <ElSelect v-model="form.status" placeholder="请选择在职状态" style="width: 100%">
          <ElOption
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton :disabled="submitting" @click="cancel">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="submit">保存</ElButton>
    </template>
  </ElDialog>
</template>
