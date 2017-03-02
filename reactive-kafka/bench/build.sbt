name := s"${(name in Global).value}-bench"

enablePlugins(JmhPlugin)

coverageExcludedPackages := ".*"