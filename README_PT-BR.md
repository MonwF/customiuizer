# CustoMIUIzer A14

**米客 A14** é um módulo de personalização mantido de forma independente para **HyperOS 1 / Android 14**. Ele usa o [MonwF/customiuizer v24.10.12](https://github.com/MonwF/customiuizer/releases/tag/v24.10.12) como referência funcional do A14, mas possui pacote, versões, compilação e validação próprios.

Suas duas diferenças centrais são a integração com a **API 101 do libxposed** e a otimização contínua de **código e recursos**. Hooks frequentes, threads, caches, reflexão e limites de erro são refinados sem sacrificar a ativação confiável após reiniciar.

> [!WARNING]
> Compatível apenas com Android 14 (SDK 34) e `arm64-v8a`. Não ative no Android 15/16 nem junto com outro módulo derivado do CustoMIUIzer.

## Estado atual

- Versão estável: r14.1.3
- Pacote: `name.monwf.customiuizer.r14`
- Base do LSPosed: [Vector v2.0-3046](https://github.com/JingMatrix/Vector/actions/runs/29805285935), commit `9350c7c`
- Lançamentos: [tomthenpc/customiuizer-a14](https://github.com/tomthenpc/customiuizer-a14/releases)

O r14.1.3 passou nos testes de abertura do aplicativo, reinicialização completa e nas funções de Launcher/SystemUI antes afetadas. Os logs mais recentes não mostram crash, ANR ou encerramento do aplicativo ou do SystemUI.

## Destaques

- Detecção de callbacks `after` compatível com ofuscação R8 para Launcher e SystemUI
- Separação entre callbacks Xposed e a inicialização normal do aplicativo
- Remoção de downloads, repositório, doações, páginas web internas e permissão de rede
- Executor compartilhado e cache de ícones com limites definidos
- Menos processamento no visualizador de áudio e menos comparação de bitmaps na thread principal

A compatibilidade depende das versões dos aplicativos de sistema Xiaomi e da ROM. Consulte [CHANGELOG.md](CHANGELOG.md) para o histórico completo.

## Instalação

1. Faça backup das configurações na versão instalada.
2. Remova outras versões oficiais ou derivadas; não ative duas cópias ao mesmo tempo.
3. Instale o APK, ative o módulo no LSPosed e confirme o escopo.
4. Abra o aplicativo uma vez e reinicie completamente o aparelho.
5. Teste o aplicativo, SystemUI, launcher, tela de bloqueio e as funções usadas no dia a dia.

## Origem e licença

Este é um projeto derivado com manutenção independente, sem afiliação ou endosso dos autores originais. Ele deriva de [Mikanoshi/CustoMIUIzer](https://code.highspec.ru/Mikanoshi/CustoMIUIzer) e do trabalho para Android 14 em [MonwF/customiuizer](https://github.com/MonwF/customiuizer).

Distribuído sob a [GPL-3.0](LICENSE). Consulte [NOTICE.md](NOTICE.md).

[English](README_en.md) | [日本語](README_jp.md) | **Português (Brasil)** | [简体中文](README.md)
