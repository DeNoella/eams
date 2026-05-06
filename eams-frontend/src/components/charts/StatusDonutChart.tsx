import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts'
import { useChartTheme } from './useChartTheme'

export interface DonutPoint {
  name: string
  value: number
  color?: string
}

const fallbackData: DonutPoint[] = [
  { name: 'No Data', value: 1, color: '#D4C4A8' },
]

export default function StatusDonutChart({ data }: { data: DonutPoint[] }) {
  const t = useChartTheme()
  const points = data.length > 0 ? data : fallbackData
  return (
    <ResponsiveContainer width="100%" height={250}>
      <PieChart>
        <Pie
          data={points}
          cx="50%"
          cy="50%"
          innerRadius={60}
          outerRadius={90}
          paddingAngle={2}
          dataKey="value"
        >
          {points.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={entry.color || '#6B2D0E'} />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{ backgroundColor: t.tooltipBg, border: `1px solid ${t.grid}`, borderRadius: 3, color: t.tickStrong }}
          itemStyle={{ color: t.tickStrong }}
          labelStyle={{ color: t.tickStrong }}
        />
        <Legend
          formatter={(value) => <span style={{ fontSize: '12px', color: t.tick }}>{value}</span>}
        />
      </PieChart>
    </ResponsiveContainer>
  )
}
