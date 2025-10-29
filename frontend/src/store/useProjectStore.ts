import { create } from 'zustand';
import { apiClient } from '@/utils/api';

export interface ChecklistItem {
  id: number;
  description: string;
  status: string;
  evidenceUrl?: string | null;
}

export interface MaterialItem {
  id: number;
  description: string;
  unit: string;
  quantity: number;
  referenceStandard?: string | null;
}

export interface Circuit {
  id: number;
  name: string;
  designCurrent?: number;
  breakerCurrent?: number;
  iz?: number;
  voltageDrop?: number;
  loadType?: string;
}

export interface Panel {
  id: number;
  name: string;
  panelType: string;
  circuits: Circuit[];
}

interface PanelResponseDto {
  id: number;
  name: string;
  panelType: string;
  dinRailCount?: number | null;
  supplySource?: string | null;
  notes?: string | null;
}

export interface Project {
  id: number;
  name: string;
  clientName?: string;
  address?: string;
  calcProfileCode?: string;
  voltageSystemType: string;
}

interface ProjectState {
  activeProject?: Project;
  panels: Panel[];
  checklist: ChecklistItem[];
  materials: MaterialItem[];
  loading: boolean;
  setPanels: (panels: Panel[]) => void;
  fetchProject: (id: number) => Promise<void>;
  fetchPanels: (projectId: number) => Promise<void>;
  fetchChecklist: (projectId: number) => Promise<void>;
  fetchMaterials: (projectId: number) => Promise<void>;
}

export const useProjectStore = create<ProjectState>((set) => ({
  panels: [],
  checklist: [],
  materials: [],
  loading: false,
  setPanels: (panels: Panel[]) => set({ panels }),
  fetchProject: async (id: number) => {
    set({ loading: true });
    const { data } = await apiClient.get<Project>(`/projects/${id}`);
    set({ activeProject: data, loading: false });
  },
  fetchPanels: async (projectId: number) => {
    set({ loading: true });
    const { data } = await apiClient.get<PanelResponseDto[]>(`/projects/${projectId}/panels`);
    const panels = await Promise.all(
      data.map(async (panel) => {
        const circuitsResponse = await apiClient.get<Circuit[]>(`/panels/${panel.id}/circuits`);
        return { ...panel, circuits: circuitsResponse.data };
      })
    );
    set({ panels, loading: false });
  },
  fetchChecklist: async (projectId: number) => {
    const { data } = await apiClient.get<ChecklistItem[]>(`/projects/${projectId}/checklist`);
    set({ checklist: data });
  },
  fetchMaterials: async (projectId: number) => {
    const { data } = await apiClient.get<MaterialItem[]>(`/projects/${projectId}/bom`);
    set({ materials: data });
  }
}));
