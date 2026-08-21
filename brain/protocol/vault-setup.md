---
uid: ss-protocol-vault-setup
record_kind: protocol
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - https://obsidian.md/help/Files%2Band%2Bfolders/Manage%2Bvaults
  - https://obsidian.md/help/Files%2Band%2Bfolders/How%2BObsidian%2Bstores%2Bdata
  - https://obsidian.md/help/sync-notes
tags:
  - obsidian
  - setup
---

# Vault setup

## Open the vault

In Obsidian choose Open folder as vault and select:

    C:\Users\spud0\OneDrive\Documents\ChatGPT\Modding

Then open [[brain/home|Shadow Slave project brain]].

The repository root is intentionally the vault root. Do not open brain as a nested second vault; nested vaults make authority documents and code links unreliable.

## Core-only baseline

Canvas, Graph, Backlinks, Properties, Templates, Search, and File Recovery are enabled in the shared baseline. Community plugins are not required. Obsidian may normalize its app-owned configuration files after an upgrade.

The template folder is brain/templates.

## Sync and Git

This folder already lives under OneDrive. Mark it Always keep on this device and do not also enable Obsidian Sync for the same vault. Using two live sync layers can produce conflicts.

Git should track stable notes, templates, canvases, and intentional shared settings. Device-local workspace layouts, caches, plugins, themes, and snippets are ignored under .obsidian.

## Multi-AI rule

External edits are expected: coding agents will write Markdown and Canvas files directly. Obsidian watches the vault for file changes. If an open note changes externally, review the diff before overwriting it from the editor.

