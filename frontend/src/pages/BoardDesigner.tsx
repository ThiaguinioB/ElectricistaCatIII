import { useEffect, useMemo, useState, type DragEvent } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useProjectStore } from '@/store/useProjectStore';
import { apiClient } from '@/utils/api';

const panelSchema = z.object({
  name: z.string().min(3),
  panelType: z.enum(['MAIN', 'SECTIONAL']),
  dinRailCount: z.coerce.number().min(1).max(6).optional()
});

const circuitSchema = z.object({
  name: z.string().min(3),
  demandPowerKw: z.coerce.number().positive(),
  phaseType: z.enum(['SINGLE_PHASE', 'THREE_PHASE']),
  conductorCrossSection: z.coerce.number().positive(),
  conductorMaterial: z.string().default('CU'),
  installationMethod: z.string().default('concealed_conduit')
});

type PanelFormValues = z.infer<typeof panelSchema>;
type CircuitFormValues = z.infer<typeof circuitSchema>;

export const BoardDesigner = () => {
  const { panels, fetchPanels, setPanels } = useProjectStore();
  const [projectId, setProjectId] = useState<number | null>(null);
  const panelForm = useForm<PanelFormValues>({
    resolver: zodResolver(panelSchema),
    defaultValues: {
      name: '',
      panelType: 'MAIN',
      dinRailCount: 2
    }
  });
  const circuitForm = useForm<CircuitFormValues>({
    resolver: zodResolver(circuitSchema),
    defaultValues: {
      name: '',
      demandPowerKw: 1,
      phaseType: 'SINGLE_PHASE',
      conductorCrossSection: 2.5,
      conductorMaterial: 'CU',
      installationMethod: 'concealed_conduit'
    }
  });

  useEffect(() => {
    if (projectId) {
      fetchPanels(projectId);
    }
  }, [projectId, fetchPanels]);

  const currentPanels = useMemo(() => panels, [panels]);

  const handlePanelSubmit = panelForm.handleSubmit(async (values) => {
    if (!projectId) return;
    await apiClient.post(`/projects/${projectId}/panels`, values);
    panelForm.reset({ name: '', panelType: 'SECTIONAL', dinRailCount: 2 });
    fetchPanels(projectId);
  });

  const handleCircuitSubmit = circuitForm.handleSubmit(async (values) => {
    if (!projectId || currentPanels.length === 0) return;
    const targetPanel = currentPanels[0];
    await apiClient.post(`/panels/${targetPanel.id}/circuits`, {
      ...values,
      voltage: values.phaseType === 'THREE_PHASE' ? 400 : 230,
      cosPhi: 0.9,
      efficiency: 0.9,
      lengthMeters: 10,
      groupingFactorCount: 1,
      ambientTemperature: 30,
      lighting: values.phaseType === 'SINGLE_PHASE'
    });
    fetchPanels(projectId);
  });

  const handleDragStart = (panelId: number, circuitId: number) => (event: DragEvent) => {
    event.dataTransfer.setData('application/panel-id', panelId.toString());
    event.dataTransfer.setData('application/circuit-id', circuitId.toString());
  };

  const handleDrop = (panelId: number, index: number) => (event: DragEvent) => {
    event.preventDefault();
    const draggedPanelId = Number(event.dataTransfer.getData('application/panel-id'));
    const draggedCircuitId = Number(event.dataTransfer.getData('application/circuit-id'));
    if (!draggedPanelId || !draggedCircuitId) {
      return;
    }
    const newPanels = panels.map((panel) => {
      if (panel.id !== panelId) {
        if (panel.id === draggedPanelId) {
          return {
            ...panel,
            circuits: panel.circuits.filter((circuit) => circuit.id !== draggedCircuitId)
          };
        }
        return panel;
      }
      const draggedCircuit = panels
        .find((p) => p.id === draggedPanelId)?.circuits.find((c) => c.id === draggedCircuitId);
      if (!draggedCircuit) {
        return panel;
      }
      const filtered = panel.circuits.filter((c) => c.id !== draggedCircuitId);
      filtered.splice(index, 0, draggedCircuit);
      return { ...panel, circuits: filtered };
    });
    setPanels(newPanels);
  };

  const allowDrop = (event: DragEvent) => {
    event.preventDefault();
  };

  return (
    <div className="space-y-8">
      <section className="rounded-xl bg-white p-6 shadow">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <div className="flex flex-col">
            <label className="text-sm font-medium text-slate-700">ID Proyecto</label>
            <input
              type="number"
              className="rounded-md border border-slate-300 px-3 py-2"
              value={projectId ?? ''}
              onChange={(event) => setProjectId(Number(event.target.value))}
              placeholder="1"
            />
          </div>
          <form onSubmit={handlePanelSubmit} className="flex flex-1 flex-wrap gap-3">
            <div className="flex flex-col">
              <label className="text-sm">Nombre tablero</label>
              <input
                {...panelForm.register('name')}
                className="rounded-md border border-slate-300 px-3 py-2"
              />
            </div>
            <div className="flex flex-col">
              <label className="text-sm">Tipo</label>
              <select
                {...panelForm.register('panelType')}
                className="rounded-md border border-slate-300 px-3 py-2"
              >
                <option value="MAIN">Principal</option>
                <option value="SECTIONAL">Seccional</option>
              </select>
            </div>
            <div className="flex flex-col">
              <label className="text-sm">Carriles DIN</label>
              <input
                type="number"
                {...panelForm.register('dinRailCount', { valueAsNumber: true })}
                className="rounded-md border border-slate-300 px-3 py-2"
              />
            </div>
            <button type="submit" className="self-end rounded-md bg-primary px-3 py-2 text-white">
              Añadir tablero
            </button>
          </form>
        </div>
      </section>

      {projectId && (
        <section className="grid gap-6 md:grid-cols-2">
          <div className="rounded-xl bg-white p-6 shadow">
            <h3 className="mb-4 text-lg font-semibold text-slate-900">Circuitos</h3>
            <form onSubmit={handleCircuitSubmit} className="grid gap-3 sm:grid-cols-2">
              <div className="flex flex-col">
                <label className="text-sm">Nombre</label>
                <input
                  {...circuitForm.register('name')}
                  className="rounded-md border border-slate-300 px-3 py-2"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm">Potencia (kW)</label>
                <input
                  type="number"
                  step="0.1"
                  {...circuitForm.register('demandPowerKw', { valueAsNumber: true })}
                  className="rounded-md border border-slate-300 px-3 py-2"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm">Fases</label>
                <select
                  {...circuitForm.register('phaseType')}
                  className="rounded-md border border-slate-300 px-3 py-2"
                >
                  <option value="SINGLE_PHASE">Monofásico</option>
                  <option value="THREE_PHASE">Trifásico</option>
                </select>
              </div>
              <div className="flex flex-col">
                <label className="text-sm">Sección (mm²)</label>
                <input
                  type="number"
                  step="0.5"
                  {...circuitForm.register('conductorCrossSection', { valueAsNumber: true })}
                  className="rounded-md border border-slate-300 px-3 py-2"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm">Material</label>
                <input
                  {...circuitForm.register('conductorMaterial')}
                  className="rounded-md border border-slate-300 px-3 py-2"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm">Instalación</label>
                <input
                  {...circuitForm.register('installationMethod')}
                  className="rounded-md border border-slate-300 px-3 py-2"
                />
              </div>
              <button
                type="submit"
                className="sm:col-span-2 rounded-md bg-accent px-3 py-2 text-white"
              >
                Añadir circuito al primer tablero
              </button>
            </form>
          </div>

          <div className="rounded-xl bg-white p-6 shadow">
            <h3 className="mb-4 text-lg font-semibold text-slate-900">Vista unifilar</h3>
            <div className="space-y-4">
              {currentPanels.map((panel) => (
                <div key={panel.id} className="rounded border border-slate-200">
                  <header className="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-4 py-2">
                    <span className="font-medium text-slate-800">{panel.name}</span>
                    <span className="text-xs uppercase text-slate-500">{panel.panelType}</span>
                  </header>
                  <ul className="divide-y divide-slate-200">
                    {panel.circuits.map((circuit, index) => (
                      <li
                        key={circuit.id}
                        className="flex items-center justify-between px-4 py-3"
                        draggable
                        onDragStart={handleDragStart(panel.id, circuit.id)}
                        onDragOver={allowDrop}
                        onDrop={handleDrop(panel.id, index)}
                      >
                        <div>
                          <p className="font-medium text-slate-800">{circuit.name}</p>
                          <p className="text-xs text-slate-500">
                            IA {circuit.designCurrent?.toFixed(2) ?? '—'} A · ΔV{' '}
                            {circuit.voltageDrop?.toFixed(2) ?? '—'}%
                          </p>
                        </div>
                        <span className="text-xs text-slate-500">{circuit.loadType ?? 'Circuito'}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}
    </div>
  );
};
