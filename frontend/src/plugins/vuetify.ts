import '@mdi/font/css/materialdesignicons.css';
import 'vuetify/styles';

import { createVuetify } from 'vuetify';
import { aliases, mdi } from 'vuetify/iconsets/mdi';

export default createVuetify({
  theme: {
    defaultTheme: 'plantAssure',
    themes: {
      plantAssure: {
        dark: false,
        colors: {
          background: '#F8F5EC',
          surface: '#FFFDF8',
          'surface-variant': '#F1EDE2',
          primary: '#173F2E',
          'primary-darken-1': '#204D3A',
          secondary: '#456457',
          accent: '#C97943',
          error: '#A54A3F',
          'on-background': '#173F2E',
          'on-surface': '#173F2E',
          'on-primary': '#FFFDF8',
          'on-secondary': '#FFFDF8',
          'on-accent': '#173F2E',
          'on-error': '#FFFDF8',
        },
      },
    },
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
  defaults: {
    VBtn: {
      elevation: 0,
      rounded: 'lg',
    },
    VCard: {
      elevation: 0,
      rounded: 'lg',
    },
  },
});
