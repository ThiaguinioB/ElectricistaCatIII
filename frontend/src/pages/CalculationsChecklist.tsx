import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { apiClient } from '@/utils/api';
import { useTranslation } from 'react-i18next';
import { useProjectStore } from '@/store/useProjectStore';

const iaSchema = z.object({
  powerKw: z.coerce.number().positive(),
  voltage: z.coerce.number().positive().default(230),
  powerFactor: z.coerce.number().min(0.5).max(1).default(0.9),
  efficiency: z.coerce.number().min(0.5).max(1).default(0.9),
  threePhase: z.boolean().optional()
});

const inSchema = z.object({
  designCurrent: z.coerce.number().positive(),
  preferredCurve: z.string().optional()
});

const izSchema = z.object({
  crossSectionMm2: z.coerce.number().positive(),
  installationMethod: z.string().nonempty(),
  material: z.string().default('CU'),
  ambientTemperature: z.coerce.number().min(0).max(60).optional(),
  groupingCount: z.coerce.number().min(1).max(10).optional()
});

const voltageDropSchema = z.object({
  current: z.coerce.number().positive(),
  lengthMeters: z.coerce.number().positive(),
  crossSectionMm2: z.coerce.number().positive(),
  material: z.string().default('CU'),
  voltage: z.coerce.number().positive().default(230),
  threePhase: z.boolean().optional(),
  powerFactor: z.coerce.number().min(0.5).max(1).default(0.9)
});

const earthingSchema = z.object({
  resistance: z.coerce.number().positive(),
  measuredAt: z.string().nonempty(),
  instrument: z.string().optional(),
  notes: z.string().optional()
});

type IaForm = z.infer<typeof iaSchema>;
type InForm = z.infer<typeof inSchema>;
type IzForm = z.infer<typeof izSchema>;
type VoltageDropForm = z.infer<typeof voltageDropSchema>;
type EarthingForm = z.infer<typeof earthingSchema>;

export const CalculationsChecklist = () => {
  const { t } = useTranslation();
  const { fetchMaterials } = useProjectStore();
  const [projectId, setProjectId] = useState<number | null>(null);
  const [results, setResults] = useState<string[]>([]);
  const iaForm = useForm<IaForm>({ resolver: zodResolver(iaSchema) });
  const inForm = useForm<InForm>({ resolver: zodResolver(inSchema) });
  const izForm = useForm<IzForm>({ resolver: zodResolver(izSchema) });
  const vdForm = useForm<VoltageDropForm>({ resolver: zodResolver(voltageDropSchema) });
  const earthingForm = useForm<EarthingForm>({
    resolver: zodResolver(earthingSchema),
    defaultValues: {
      measuredAt: new Date().toISOString()
    }
  });

  const appendResult = (label: string, value: number, unit: string) => {
    setResults((prev) => [`${label}: ${value.toFixed(2)} ${unit}`, ...prev.slice(0, 4)]);
  };

  const handleIa = iaForm.handleSubmit(async (values) => {
    const { data } = await apiClient.post('/calculations/ia', values);
    appendResult('IA', data.result, data.unit);
  });

  const handleIn = inForm.handleSubmit(async (values) => {
    const { data } = await apiClient.post('/calculations/in', values);
    appendResult('IN', data.result, data.unit);
  });

  const handleIz = izForm.handleSubmit(async (values) => {
    const { data } = await apiClient.post('/calculations/iz', values);
    appendResult('IZ', data.result, data.unit);
  });

  const handleVoltageDrop = vdForm.handleSubmit(async (values) => {
    const { data } = await apiClient.post('/calculations/voltage-drop', values);
    const percent = data.metadata?.percent ?? 0;
    setResults((prev) => [`ΔV: ${percent.toFixed(2)} %`, ...prev.slice(0, 4)]);
  });

  const handleEarthing = earthingForm.handleSubmit(async (values) => {
    if (!projectId) return;
    await apiClient.post(`/earthing/${projectId}/measurements`, values);
    earthingForm.reset({
      resistance: undefined,
      measuredAt: new Date().toISOString(),
      instrument: '',
      notes: ''
    });
    fetchMaterials(projectId);
  });

  return (
    <div className="space-y-8">
      <section className="grid gap-6 md:grid-cols-2">
        <div className="rounded-xl bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-slate-900">Cálculo IA</h3>
          <form onSubmit={handleIa} className="grid gap-3">
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Potencia kW"
              type="number"
              step="0.1"
              {...iaForm.register('powerKw', { valueAsNumber: true })}
            />
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Voltaje"
              type="number"
              {...iaForm.register('voltage', { valueAsNumber: true })}
            />
            <button className="rounded-md bg-primary px-3 py-2 text-white" type="submit">
              Calcular IA
            </button>
          </form>
        </div>

        <div className="rounded-xl bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-slate-900">Selección IN</h3>
          <form onSubmit={handleIn} className="grid gap-3">
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Corriente de diseño"
              type="number"
              {...inForm.register('designCurrent', { valueAsNumber: true })}
            />
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Curva preferida (B/C/D)"
              {...inForm.register('preferredCurve')}
            />
            <button className="rounded-md bg-primary px-3 py-2 text-white" type="submit">
              Calcular IN
            </button>
          </form>
        </div>

        <div className="rounded-xl bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-slate-900">Capacidad IZ</h3>
          <form onSubmit={handleIz} className="grid gap-3">
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Sección mm²"
              type="number"
              step="0.5"
              {...izForm.register('crossSectionMm2', { valueAsNumber: true })}
            />
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Método instalación"
              {...izForm.register('installationMethod')}
            />
            <button className="rounded-md bg-primary px-3 py-2 text-white" type="submit">
              Calcular IZ
            </button>
          </form>
        </div>

        <div className="rounded-xl bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-slate-900">Caída de tensión</h3>
          <form onSubmit={handleVoltageDrop} className="grid gap-3">
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Corriente (A)"
              type="number"
              {...vdForm.register('current', { valueAsNumber: true })}
            />
            <input
              className="rounded-md border border-slate-300 px-3 py-2"
              placeholder="Longitud (m)"
              type="number"
              {...vdForm.register('lengthMeters', { valueAsNumber: true })}
            />
            <button className="rounded-md bg-primary px-3 py-2 text-white" type="submit">
              Calcular ΔV
            </button>
          </form>
        </div>
      </section>

      <section className="grid gap-6 md:grid-cols-2">
        <div className="rounded-xl bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-slate-900">Resultados recientes</h3>
          <ul className="space-y-2 text-sm text-slate-700">
            {results.map((result, index) => (
              <li key={index} className="rounded border border-slate-200 px-3 py-2">
                {result}
              </li>
            ))}
          </ul>
        </div>

        <div className="rounded-xl bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-slate-900">Puesta a tierra</h3>
          <div className="mb-4 flex items-end gap-3">
            <div className="flex flex-col">
              <label className="text-sm text-slate-600">ID proyecto</label>
              <input
                type="number"
                value={projectId ?? ''}
                onChange={(event) => setProjectId(Number(event.target.value))}
                className="rounded-md border border-slate-300 px-3 py-2"
              />
            </div>
          </div>
          <form onSubmit={handleEarthing} className="grid gap-3">
            <input
              type="number"
              step="0.01"
              placeholder="Resistencia (Ω)"
              className="rounded-md border border-slate-300 px-3 py-2"
              {...earthingForm.register('resistance', { valueAsNumber: true })}
            />
            <input
              type="datetime-local"
              className="rounded-md border border-slate-300 px-3 py-2"
              {...earthingForm.register('measuredAt')}
            />
            <input
              placeholder="Instrumento"
              className="rounded-md border border-slate-300 px-3 py-2"
              {...earthingForm.register('instrument')}
            />
            <textarea
              placeholder="Notas"
              className="rounded-md border border-slate-300 px-3 py-2"
              rows={2}
              {...earthingForm.register('notes')}
            />
            <button type="submit" className="rounded-md bg-accent px-3 py-2 text-white">
              Registrar medición
            </button>
          </form>
        </div>
      </section>
    </div>
  );
};
