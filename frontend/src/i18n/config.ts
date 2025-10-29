import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

const resources = {
  es: {
    translation: {
      dashboard: 'Panel de proyectos',
      boardDesigner: 'Diseñador de tableros',
      calculations: 'Cálculos y checklist',
      createProject: 'Crear proyecto',
      client: 'Cliente',
      address: 'Dirección',
      standardsProfile: 'Perfil normativo',
      bom: 'Listado de materiales',
      measurements: 'Mediciones',
      add: 'Agregar',
      export: 'Exportar',
      report: 'Generar reporte'
    }
  },
  en: {
    translation: {
      dashboard: 'Project dashboard',
      boardDesigner: 'Board designer',
      calculations: 'Calculations & checklist',
      createProject: 'Create project',
      client: 'Client',
      address: 'Address',
      standardsProfile: 'Standards profile',
      bom: 'Bill of materials',
      measurements: 'Measurements',
      add: 'Add',
      export: 'Export',
      report: 'Generate report'
    }
  }
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'es',
    interpolation: {
      escapeValue: false
    }
  });

export default i18n;
