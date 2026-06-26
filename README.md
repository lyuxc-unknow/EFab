# EFab
Fabrication

## CraftTweaker Compatibility

CraftTweaker scripts go in `run/scripts/*.zs`.

EFab exposes two recipe entry points:

- `EFabRecipe.addShaped(output, matrix)`
- `EFabRecipe.addShapeless(output, ingredients)`

Both return a chainable builder supporting:

- `.time(ticks)`
- `.tier("tier")`
- `.tiers(["tier_a", "tier_b"])`
- `.fePerTick(fe)`
- `.fluid(<fluid:minecraft:water> * amount)`

Valid tiers are `gearbox`, `steam`, `liquid`, `fe`, `computing`, `mana`,
`upgrade_magic`, `upgrade_armory`, `upgrade_power`, and `upgrade_digital`.

```zenscript
EFabRecipe.addShaped(<item:minecraft:stone_pickaxe>, [
    [<item:minecraft:diamond>],
    [<item:minecraft:gold_ingot>],
    [<item:minecraft:stick>]
])
    .time(50)
    .tier("liquid")
    .fluid(<fluid:minecraft:water> * 500);

EFabRecipe.addShapeless("crt_glowstone_test", <item:minecraft:glowstone_dust>, [
    <item:minecraft:redstone>
])
    .time(100)
    .tier("fe")
    .fePerTick(10);
```

## KubeJS Compatibility

KubeJS server scripts go in `run/kubejs/server_scripts/*.js`.

EFab exposes two recipe functions:

- `event.recipes.efab.grid_shaped(output, pattern, key)`
- `event.recipes.efab.grid_shapeless(output, ingredients)`

Both support the same chainable settings as CraftTweaker. Fluid requirements use
the EFab string form: `<amount>x <fluid id>`.

```js
ServerEvents.recipes(event => {
  event.recipes.efab.grid_shaped(
    'minecraft:stone_pickaxe',
    [
      'D',
      'G',
      'S'
    ],
    {
      D: 'minecraft:diamond',
      G: 'minecraft:gold_ingot',
      S: 'minecraft:stick'
    }
  )
    .time(50)
    .tier('liquid')
    .fluid('500x minecraft:water')
    .id('efab:kubejs_stone_pickaxe_test');

  event.recipes.efab.grid_shapeless(
    'minecraft:glowstone_dust',
    [
      'minecraft:redstone'
    ]
  )
    .time(100)
    .tier('fe')
    .fePerTick(10)
    .id('efab:kubejs_glowstone_test');

  event.recipes.efab.grid_shaped(
    'minecraft:diamond_block',
    [
      'ccc',
      'csc',
      'ccc'
    ],
    {
      c: 'minecraft:cobblestone',
      s: 'minecraft:stone'
    }
  )
    .time(60)
    .tiers(['steam', 'liquid'])
    .fluid('1000x minecraft:water')
    .fluid('1000x minecraft:lava')
    .id('efab:kubejs_diamond_block_test');
});
```
