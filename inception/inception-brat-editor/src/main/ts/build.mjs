/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import yargs from 'yargs/yargs'
import { hideBin } from 'yargs/helpers'
import esbuild from 'esbuild'
import { sassPlugin } from 'esbuild-sass-plugin'
import fs from 'fs-extra'

const argv = yargs(hideBin(process.argv)).argv

const packagePath = 'de/tudarmstadt/ukp/clarin/webanno/brat/resource'

let outbase = `../../../target/js/${packagePath}`

const defaults = {
  bundle: true,
  sourcemap: true,
  minify: !argv.live,
  target: 'es6',
  loader: { '.ts': 'ts' },
  logLevel: 'info',
  plugins: [sassPlugin()]
}

if (argv.live) {
  defaults.watch = {
    onRebuild (error, result) {
      if (error) console.error('watch build failed:', error)
      else console.log('watch build succeeded:', result)
    }
  }
  outbase = `../../../target/classes/${packagePath}`
} else {
  fs.emptyDirSync(outbase)
}
fs.mkdirsSync(`${outbase}`)

esbuild.build(Object.assign({
  entryPoints: ['src/brat.ts'],
  outfile: `${outbase}/brat.min.js`,
  globalName: 'Brat',
}, defaults))

esbuild.build(Object.assign({
  entryPoints: ['src/brat_curation.ts'],
  outfile: `${outbase}/brat_curation.min.js`,
  globalName: 'BratCuration',
}, defaults))
