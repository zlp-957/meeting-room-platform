module.exports = {
  presets: [
    '@vue/cli-plugin-babel/preset'
  ],
  plugins: [
    // Element UI 按需引入（B-19）：主入口仍引入 index.css，但 JS 按组件拆分加载
    ['component', {
      libraryName: 'element-ui',
      styleLibraryName: 'theme-chalk'
    }]
  ]
}
