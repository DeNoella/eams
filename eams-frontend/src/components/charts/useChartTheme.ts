import { useEffect, useState } from 'react'

interface ChartTheme {
  grid: string
  tick: string
  tickStrong: string
  tooltipBg: string
}

function read(): ChartTheme {
  if (typeof document === 'undefined') {
    return { grid: '#D4C4A8', tick: '#3D2B1F', tickStrong: '#2C1810', tooltipBg: '#FFFFFF' }
  }
  const isDark = document.documentElement.getAttribute('data-theme') === 'dark'
  return isDark
    ? { grid: '#5A3A2A', tick: '#C8B89A', tickStrong: '#F5F0E8', tooltipBg: '#3A2218' }
    : { grid: '#D4C4A8', tick: '#3D2B1F', tickStrong: '#2C1810', tooltipBg: '#FFFFFF' }
}

export function useChartTheme(): ChartTheme {
  const [theme, setTheme] = useState<ChartTheme>(() => read())

  useEffect(() => {
    const obs = new MutationObserver(() => setTheme(read()))
    obs.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
    return () => obs.disconnect()
  }, [])

  return theme
}
