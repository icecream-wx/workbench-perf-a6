import { get, request } from './http';

export type EmployeeStatus = '在职' | '离职';

export interface Employee {
  id: number;
  code: string;
  name: string;
  department: string | null;
  position: string | null;
  phone: string | null;
  hireDate: string | null;
  status: EmployeeStatus;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeePayload {
  code: string;
  name: string;
  department: string | null;
  position: string | null;
  phone: string | null;
  hireDate: string | null;
  status: EmployeeStatus;
}

export interface EmployeeQuery {
  page: number;
  size: number;
  keyword?: string;
}

export interface EmployeePage {
  list: Employee[];
  total: number;
  page: number;
  size: number;
}

export function listEmployees(query: EmployeeQuery): Promise<EmployeePage> {
  return get<EmployeePage>('/employees', { params: query });
}

export function getEmployee(id: number): Promise<Employee> {
  return get<Employee>(`/employees/${id}`);
}

export function createEmployee(payload: EmployeePayload): Promise<Employee> {
  return request<Employee>({ method: 'POST', url: '/employees', data: payload });
}

export function updateEmployee(id: number, payload: EmployeePayload): Promise<Employee> {
  return request<Employee>({ method: 'PUT', url: `/employees/${id}`, data: payload });
}
