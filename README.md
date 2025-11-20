# Source Block Mod | 源方块模组

[English](#english) | [中文](#中文)

---

## English

A Minecraft mod that adds infinite source blocks for water, lava, and milk, along with a void block that destroys fluids, chemicals, and energy.

### Features

#### 📦 Block Types

- **Empty Source Block** (Void Block)
  - Destroys all input fluids (infinite capacity)
  - Destroys all input energy (FE/RF compatible)
  - Destroys all input chemicals (Mekanism gases, slurries, infusions, pigments)
  - Perfect for waste disposal and load balancing

- **Water Source Block**
  - Provides infinite water
  - Capacity: `Integer.MAX_VALUE` (2,147,483,647 mB)
  - Auto-output to adjacent blocks

- **Lava Source Block**
  - Provides infinite lava
  - Capacity: `Integer.MAX_VALUE` (2,147,483,647 mB)
  - Auto-output to adjacent blocks

- **Milk Source Block**
  - Provides infinite milk
  - Automatically detects milk fluids from mods (Create, Thermal, etc.)
  - Capacity: `Integer.MAX_VALUE` (2,147,483,647 mB)
  - Auto-output to adjacent blocks

#### ⚡ Smart Output System

- Attempts to output fluids to all 6 adjacent blocks every 20 ticks
- When output succeeds, switches to 1 tick/output for that face
- When output fails, switches back to 20 ticks/output
- Each face operates independently
- Output amount: `Integer.MAX_VALUE` per transfer (instant fill)

#### 🔧 Obtaining Blocks

1. **Empty Source Block**
   - Craft with iron ingots and buckets

2. **Water Source Block**
   - Shapeless crafting: Empty Source Block + Water Bucket
   - Right-click empty source block with water bucket

3. **Lava Source Block**
   - Shapeless crafting: Empty Source Block + Lava Bucket
   - Right-click empty source block with lava bucket

4. **Milk Source Block**
   - Right-click a cow while holding an empty source block
   - The cow will be removed and you'll receive a milk source block

#### 🔌 Mod Compatibility

- **Mekanism** (Optional)
  - Empty source block can destroy all chemical types
  - Supports gases, slurries, infusions, and pigments
  - Automatically detected when Mekanism is installed

- **Create & Other Mods**
  - Automatically detects and provides milk fluids
  - Compatible with all fluid-based mods using NeoForge Fluid API
  - Compatible with all energy mods using FE/RF standard

### Technical Details

- **Mod Version**: 1.0.0
- **Minecraft Version**: 1.21.1
- **Mod Loader**: NeoForge 21.1.215+
- **Java Version**: 21

### Recipes

#### Empty Source Block
```
[I] [B] [I]
[B] [ ] [B]
[I] [B] [I]

I = Iron Ingot
B = Bucket
```

#### Water/Lava Source Blocks
- Shapeless: Empty Source Block + Water/Lava Bucket

### Credits

- Developed for Minecraft 1.21.1 with NeoForge
- Mekanism API integration for chemical handling

---

## 中文

一个Minecraft模组，添加了水、岩浆和牛奶的无限源方块，以及可以销毁流体、化学物质和能量的虚空方块。

### 功能特性

#### 📦 方块类型

- **空源方块**
  - 销毁输入的流体
  - 销毁输入的能量（兼容FE/RF）
  - 销毁输入的化学物质（Mekanism的气体、浆液、注入、颜料）

- **水源方块**
  - 提供无限水源
  - 容量：`Integer.MAX_VALUE` (2,147,483,647 mB)
  - 自动向相邻方块输出

- **岩浆源方块**
  - 提供无限岩浆源
  - 容量：`Integer.MAX_VALUE` (2,147,483,647 mB)
  - 自动向相邻方块输出

- **牛奶源方块**
  - 提供无限牛奶
  - 自动检测模组的牛奶流体（机械动力、热力系列等）
  - 容量：`Integer.MAX_VALUE` (2,147,483,647 mB)
  - 自动向相邻方块输出

#### ⚡ 智能输出系统

- 每20tick尝试向周围6个面的方块输出流体
- 输出成功时，该面切换为每tick输出
- 输出失败时，恢复为每20tick输出
- 每个面独立运作
- 输出量：每次传输 `Integer.MAX_VALUE`

#### 🔧 获取方式

1. **空源方块**
   - 使用铁锭和桶合成

2. **水源方块**
   - 无序合成：空源方块 + 水桶
   - 用水桶右键空源方块

3. **岩浆源方块**
   - 无序合成：空源方块 + 岩浆桶
   - 用岩浆桶右键空源方块

4. **牛奶源方块**
   - 手持空源方块右键牛
   - 牛会被移除，你将获得牛奶源方块

#### 🔌 模组兼容性

- **Mekanism**（可选）
  - 空源方块可以销毁所有化学物质类型
  - 支持气体、浆液、注入液和颜料
  - 安装Mekanism时自动检测并启用

- **机械动力及其他模组**
  - 自动检测并提供牛奶流体
  - 兼容所有使用NeoForge流体API的模组
  - 兼容所有使用FE/RF标准的能量模组

### 技术信息

- **模组版本**：1.0.0
- **Minecraft版本**：1.21.1
- **模组加载器**：NeoForge 21.1.215+
- **Java版本**：21

### 合成配方

#### 空源方块
```
[铁] [桶] [铁]
[桶] [空] [桶]
[铁] [桶] [铁]

铁 = 铁锭
桶 = 桶
```

#### 水/岩浆源方块
- 无序合成：空源方块 + 水桶/岩浆桶

### 制作人员

- 为Minecraft 1.21.1和NeoForge开发
- 集成Mekanism API实现化学物质处理

---

## License | 许可证

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

本项目采用MIT许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

## Links | 链接

- NeoForged Documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/
