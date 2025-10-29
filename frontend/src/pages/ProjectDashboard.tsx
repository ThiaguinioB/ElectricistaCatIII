import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useProjectStore } from '@/store/useProjectStore';
import { useTranslation } from 'react-i18next';
import { apiClient, downloadBlob } from '@/utils/api';

const projectSchema = z.object({
  name: z.string().min(3),
  clientName: z.string().optional(),
  address: z.string().optional(),
  description: z.string().optional(),
  voltageSystemType: z.enum(['SINGLE_PHASE', 'THREE_PHASE', 'MIXED'])
});

const checklistSchema = z.object({
  description: z.string().min(5),
  status: z.enum(['PENDING', 'IN_PROGRESS', 'DONE'])
});

type ProjectFormValues = z.infer<typeof projectSchema>;
type ChecklistFormValues = z.infer<typeof checklistSchema>;

export const ProjectDashboard = () => {
  const { t } = useTranslation();
  const { activeProject, fetchProject, fetchMaterials, materials, checklist, fetchChecklist } =
    useProjectStore();
  const [projectId, setProjectId] = useState<number | null>(null);
  const projectForm = useForm<ProjectFormValues>({
    resolver: zodResolver(projectSchema),
    defaultValues: {
      name: '',
      clientName: '',
      address: '',
      description: '',
      voltageSystemType: 'SINGLE_PHASE'
    }
  });
  const checklistForm = useForm<ChecklistFormValues>({
    resolver: zodResolver(checklistSchema),
    defaultValues: {
      description: '',
      status: 'PENDING'
    }
  });

  useEffect(() => {
    if (projectId) {
      fetchProject(projectId).then(() => {
        fetchMaterials(projectId);
        fetchChecklist(projectId);
      });
    }
  }, [projectId, fetchProject, fetchMaterials, fetchChecklist]);

  const handleCreateProject = projectForm.handleSubmit(async (values) => {
    const { data } = await apiClient.post('/projects', values);
    setProjectId(data.id);
  });

  const handleAddChecklist = checklistForm.handleSubmit(async (values) => {
    if (!projectId) return;
    await apiClient.post(`/projects/${projectId}/checklist`, values);
    checklistForm.reset();
    fetchChecklist(projectId);
  });

  const handleExportBom = async () => {
    if (!projectId) return;
    const response = await apiClient.get(`/projects/${projectId}/bom`, {
      params: { format: 'csv' },
      responseType: 'blob'
    });
    downloadBlob(response.data, `project-${projectId}-bom.csv`, 'text/csv');
  };

  const handleGenerateReport = async () => {
    if (!projectId) return;
    const response = await apiClient.post(`/projects/${projectId}/report`, {}, { responseType: 'blob' });
    downloadBlob(response.data, `project-${projectId}-dossier.pdf`, 'application/pdf');
  };

  return (
    <div className="space-y-8">
      <section className="rounded-xl bg-white p-6 shadow">
        <h2 className="text-lg font-semibold text-slate-900">{t('createProject')}</h2>
        <form onSubmit={handleCreateProject} className="mt-4 grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col">
            <label className="text-sm font-medium text-slate-700">Nombre</label>
            <input
              {...projectForm.register('name')}
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Residencial ..."
            />
            {projectForm.formState.errors.name && (
              <span className="text-sm text-red-600">{projectForm.formState.errors.name.message}</span>
            )}
          </div>
          <div className="flex flex-col">
            <label className="text-sm font-medium text-slate-700">{t('client')}</label>
            <input
              {...projectForm.register('clientName')}
              className="rounded-md border border-slate-300 px-3 py-2"
            />
          </div>
          <div className="flex flex-col">
            <label className="text-sm font-medium text-slate-700">{t('address')}</label>
            <input
              {...projectForm.register('address')}
              className="rounded-md border border-slate-300 px-3 py-2"
            />
          </div>
          <div className="flex flex-col">
            <label className="text-sm font-medium text-slate-700">Tipo de sistema</label>
            <select
              {...projectForm.register('voltageSystemType')}
              className="rounded-md border border-slate-300 px-3 py-2"
            >
              <option value="SINGLE_PHASE">Monofásico</option>
              <option value="THREE_PHASE">Trifásico</option>
              <option value="MIXED">Mixto</option>
            </select>
          </div>
          <div className="sm:col-span-2 flex flex-col">
            <label className="text-sm font-medium text-slate-700">Descripción</label>
            <textarea
              {...projectForm.register('description')}
              className="rounded-md border border-slate-300 px-3 py-2"
              rows={3}
            />
          </div>
          <div className="sm:col-span-2 flex justify-end">
            <button
              type="submit"
              className="rounded-md bg-primary px-4 py-2 text-white shadow hover:bg-teal-700"
            >
              {projectId ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </section>

      {activeProject && (
        <section className="grid gap-6 lg:grid-cols-3">
          <div className="rounded-xl bg-white p-6 shadow lg:col-span-2">
            <header className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900">{t('bom')}</h3>
              <button
                type="button"
                onClick={handleExportBom}
                className="rounded-md border border-primary px-3 py-2 text-sm text-primary"
              >
                {t('export')} CSV
              </button>
            </header>
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead>
                <tr className="text-left text-slate-600">
                  <th className="py-2">Descripción</th>
                  <th className="py-2">Unidad</th>
                  <th className="py-2">Cantidad</th>
                  <th className="py-2">Norma</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {materials.map((material) => (
                  <tr key={material.id}>
                    <td className="py-2">{material.description}</td>
                    <td className="py-2">{material.unit}</td>
                    <td className="py-2">{material.quantity}</td>
                    <td className="py-2">{material.referenceStandard ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="rounded-xl bg-white p-6 shadow">
            <header className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900">Checklist</h3>
              <button
                type="button"
                onClick={handleGenerateReport}
                className="rounded-md bg-accent px-3 py-2 text-sm text-white shadow"
              >
                {t('report')}
              </button>
            </header>
            <ul className="space-y-3">
              {checklist.map((item) => (
                <li key={item.id} className="rounded border border-slate-200 p-3">
                  <p className="font-medium text-slate-800">{item.description}</p>
                  <span className="text-xs uppercase tracking-wide text-primary">{item.status}</span>
                </li>
              ))}
            </ul>
            <form onSubmit={handleAddChecklist} className="mt-4 space-y-3">
              <div>
                <label className="text-sm font-medium text-slate-700">Descripción</label>
                <textarea
                  {...checklistForm.register('description')}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2"
                  rows={3}
                />
              </div>
              <div>
                <label className="text-sm font-medium text-slate-700">Estado</label>
                <select
                  {...checklistForm.register('status')}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2"
                >
                  <option value="PENDING">Pendiente</option>
                  <option value="IN_PROGRESS">En progreso</option>
                  <option value="DONE">Completado</option>
                </select>
              </div>
              <button type="submit" className="w-full rounded-md bg-primary px-3 py-2 text-white">
                {t('add')}
              </button>
            </form>
          </div>
        </section>
      )}
    </div>
  );
};
