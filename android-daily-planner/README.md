# Daily Planner (Android)

Aplicativo Android (Jetpack Compose) para montar uma agenda diária com tarefas recorrentes.

## Funcionalidades MVP
- Criar tarefas com título.
- Definir recorrência:
  - Diária
  - Só sexta-feira
  - Semanal em um dia escolhido
  - Única (com data escolhida)
- Navegar entre dias e visualizar apenas as tarefas previstas para o dia selecionado.
- Remover tarefas da lista do dia.

## Como abrir
1. Abra a pasta `android-daily-planner` no Android Studio (Giraffe+).
2. Aguarde sincronização Gradle.
3. Rode o app em emulador/dispositivo Android.

## Como usar no celular (dispositivo físico)
### Opção 1 — Rodar direto pelo Android Studio (recomendado para desenvolvimento)
1. No celular, ative **Opções do desenvolvedor**.
2. Ative **Depuração USB**.
3. Conecte o celular no computador por USB.
4. No Android Studio, selecione seu dispositivo na barra superior.
5. Clique em **Run ▶** para instalar e abrir o app.

### Opção 2 — Gerar APK e instalar manualmente
1. No Android Studio, use **Build > Build APK(s)**.
2. Ao finalizar, clique em **locate** para abrir a pasta do APK gerado.
3. Copie o APK para o celular.
4. No celular, permita instalação de apps de “fontes desconhecidas” para o app usado na instalação (gerenciador de arquivos/navegador).
5. Abra o APK e conclua a instalação.

### Opção 3 — Instalar via ADB (linha de comando)
Com o celular conectado e depuração USB ativa:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> Dica: a versão atual do projeto está com `minSdk = 24` (Android 7.0+).

## Próximos passos sugeridos
- Persistência local com Room.
- Edição/exclusão de tarefa.
- Seleção de data customizada para tarefa única.
- Notificações.
